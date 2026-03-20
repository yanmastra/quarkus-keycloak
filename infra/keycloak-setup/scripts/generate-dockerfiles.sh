#!/usr/bin/env bash
# =============================================================================
# generate-dockerfiles.sh
# Generates custom Dockerfiles for the Keycloak service.
#
# Why a custom Keycloak Dockerfile?
#   The official Keycloak image requires running `kc.sh build` before production
#   use to bake in providers, themes, and optimised startup flags. This script
#   generates that Dockerfile so the image is pre-built and fast to start.
#
# Usage:
#   ./scripts/generate-dockerfiles.sh [keycloak_version]
#
# Example:
#   ./scripts/generate-dockerfiles.sh 24.0.4
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Defaults
KC_VERSION="${1:-24.0.4}"
OUTPUT_DIR="$PROJECT_ROOT"

GREEN='\033[0;32m'; NC='\033[0m'
info() { echo -e "${GREEN}[INFO]${NC}  $*"; }

# --------------------------------------------------------------------------
# Keycloak Dockerfile
# Bakes the custom theme and runs `kc.sh build` for optimised startup
# --------------------------------------------------------------------------
cat > "$OUTPUT_DIR/Dockerfile.keycloak" <<DOCKERFILE
# =============================================================================
# Custom Keycloak Image
# Base: quay.io/keycloak/keycloak:${KC_VERSION}
# - Runs kc.sh build to produce an optimised runtime image
# - Themes are NOT baked into the image; they are mounted at runtime via a
#   host volume (keycloak/themes/ → /opt/keycloak/themes/) so you can update
#   the theme without rebuilding the image.
# =============================================================================
FROM quay.io/keycloak/keycloak:${KC_VERSION} AS builder

# Environment for build phase
ENV KC_DB=postgres
ENV KC_HTTP_RELATIVE_PATH=/
ENV KC_FEATURES=token-exchange,admin-fine-grained-authz

# Build optimised Keycloak — only DB and feature flags are valid here.
# Runtime flags (http, hostname, proxy) are passed at container start via docker-compose env.
RUN /opt/keycloak/bin/kc.sh build \
    --db=postgres \
    --features=token-exchange,admin-fine-grained-authz \
    --health-enabled=true \
    --metrics-enabled=false

# --------------------------------------------------------------------------
# Runtime stage — minimal image
# --------------------------------------------------------------------------
FROM quay.io/keycloak/keycloak:${KC_VERSION}

# Copy the pre-built optimised files from builder
COPY --from=builder /opt/keycloak/ /opt/keycloak/

# Runtime environment — sensitive values injected via docker-compose .env
ENV KC_DB=postgres
ENV KC_HTTP_ENABLED=true
ENV KC_PROXY=edge
ENV KC_LOG_LEVEL=INFO

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
DOCKERFILE

info "Generated: $OUTPUT_DIR/Dockerfile.keycloak"

# --------------------------------------------------------------------------
# Postgres init Dockerfile (optional — just uses official image)
# --------------------------------------------------------------------------
cat > "$OUTPUT_DIR/Dockerfile.postgres" <<DOCKERFILE
# =============================================================================
# PostgreSQL with custom init scripts
# Extends the official postgres image to run *.sql scripts on first start.
# The official image already supports /docker-entrypoint-initdb.d/ so
# this Dockerfile only exists as an explicit reference / future extension point.
# =============================================================================
FROM postgres:16-alpine

# Copy any custom init SQL scripts
COPY postgres/init/ /docker-entrypoint-initdb.d/

# Use a custom postgresql.conf tuned for Keycloak
COPY postgres/postgresql.conf /etc/postgresql/postgresql.conf

CMD ["postgres", "-c", "config_file=/etc/postgresql/postgresql.conf"]
DOCKERFILE

info "Generated: $OUTPUT_DIR/Dockerfile.postgres"

# --------------------------------------------------------------------------
# Postgres tuning config
# --------------------------------------------------------------------------
mkdir -p "$OUTPUT_DIR/postgres"
mkdir -p "$OUTPUT_DIR/postgres"
cat > "$OUTPUT_DIR/postgres/postgresql.conf" <<'PGCONF'
# PostgreSQL configuration tuned for Keycloak on a single VM
# Adjust max_connections and shared_buffers based on available RAM

listen_addresses = '*'
max_connections = 100

# Memory (tune for ~25% of total RAM for shared_buffers)
shared_buffers = 256MB
effective_cache_size = 768MB
work_mem = 4MB
maintenance_work_mem = 64MB

# Write-Ahead Log
wal_level = replica
max_wal_size = 1GB
min_wal_size = 80MB
checkpoint_completion_target = 0.9

# Query planner
default_statistics_target = 100
random_page_cost = 1.1

# Logging
log_timezone = 'UTC'
log_min_duration_statement = 1000
log_line_prefix = '%t [%p]: [%l-1] user=%u,db=%d,app=%a,client=%h '

# Locale
datestyle = 'iso, mdy'
timezone = 'UTC'
lc_messages = 'en_US.utf8'
lc_monetary = 'en_US.utf8'
lc_numeric = 'en_US.utf8'
lc_time = 'en_US.utf8'
default_text_search_config = 'pg_catalog.english'
PGCONF

info "Generated: $OUTPUT_DIR/postgres/postgresql.conf"

# --------------------------------------------------------------------------
# Postgres init SQL
# --------------------------------------------------------------------------
mkdir -p "$OUTPUT_DIR/postgres/init"
cat > "$OUTPUT_DIR/postgres/init/01-init.sql" <<'SQL'
-- Keycloak database initialisation
-- This script runs ONLY on first container start (when data volume is empty)

-- Create keycloak user if not exists
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'keycloak') THEN
    CREATE ROLE keycloak WITH LOGIN PASSWORD 'ChangeMe_DB123!';
  END IF;
END
$$;

-- Create database if not exists
SELECT 'CREATE DATABASE keycloak OWNER keycloak'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak')\gexec

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
SQL

info "Generated: $OUTPUT_DIR/postgres/init/01-init.sql"

echo ""
info "All Dockerfiles and Postgres config generated successfully."
info "Next: run './scripts/generate-ssl.sh' then 'docker compose up -d --build'"