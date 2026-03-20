#!/bin/bash
# =============================================================================
# Reset and re-import the Keycloak realm
# Removes the sentinel file so the entrypoint re-runs the realm import,
# then rebuilds and restarts the Keycloak container.
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

SENTINEL_FILE="$PROJECT_DIR/data/keycloak-data/.realm-imported"

if [ -f "$SENTINEL_FILE" ]; then
    echo "[INFO] Removing sentinel file: $SENTINEL_FILE"
    rm "$SENTINEL_FILE"
else
    echo "[INFO] Sentinel file not found — realm import will run on next start."
fi

echo "[INFO] Rebuilding and restarting Keycloak..."
docker compose -f "$PROJECT_DIR/docker-compose.yml" up -d --build keycloak

echo "[DONE] Keycloak is starting. Check logs with: docker compose logs -f keycloak"
