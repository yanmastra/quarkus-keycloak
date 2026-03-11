#!/usr/bin/env bash
# =============================================================================
# generate-ssl.sh
# Generates SSL certificates for Keycloak / Nginx.
# Supports two modes:
#   1. selfsigned  — OpenSSL self-signed (no domain required, good for dev/internal)
#   2. letsencrypt — Certbot Let's Encrypt (requires a real public domain + port 80 open)
#
# Usage:
#   ./scripts/generate-ssl.sh [selfsigned|letsencrypt] [domain] [email]
#
# Examples:
#   ./scripts/generate-ssl.sh selfsigned auth.example.com
#   ./scripts/generate-ssl.sh letsencrypt auth.example.com admin@example.com
# =============================================================================

set -euo pipefail

# --------------------------------------------------------------------------
# Defaults (overridden by args or .env)
# --------------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ENV_FILE="$PROJECT_ROOT/.env"

# Load .env if present
if [[ -f "$ENV_FILE" ]]; then
  export "$(grep -v '^#' "$ENV_FILE" | xargs)"
fi

MODE="${1:-${SSL_MODE:-selfsigned}}"
DOMAIN="${2:-${DOMAIN:-localhost}}"
EMAIL="${3:-admin@${DOMAIN}}"
SSL_DIR="$PROJECT_ROOT/nginx/ssl"

# --------------------------------------------------------------------------
# Colour helpers
# --------------------------------------------------------------------------
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
info()    { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*" >&2; exit 1; }

# Validate domain is set and non-empty
if [[ -z "$DOMAIN" || "$DOMAIN" == "localhost" && "$MODE" == "letsencrypt" ]]; then
  error "DOMAIN is not set. Pass it as an argument: $0 $MODE your-domain.com"
fi
info "Using domain: '$DOMAIN'"

# --------------------------------------------------------------------------
# Dependency checks
# --------------------------------------------------------------------------
require() { command -v "$1" &>/dev/null || error "Required tool not found: $1. Please install it."; }

# --------------------------------------------------------------------------
# Mode: Self-signed
# --------------------------------------------------------------------------
generate_selfsigned() {
  require openssl

  info "Generating self-signed certificate for domain: $DOMAIN"
  mkdir -p "$SSL_DIR"

  CERT_FILE="$SSL_DIR/cert.pem"
  KEY_FILE="$SSL_DIR/key.pem"
  DH_FILE="$SSL_DIR/dhparam.pem"

  # Generate private key + self-signed certificate (valid 825 days — macOS Safari limit)
  # Write OpenSSL config using printf to avoid heredoc expansion issues on macOS
  OPENSSL_CONF=$(mktemp /tmp/openssl-san-XXXXXX.cnf)

  printf '[req]\n' > "$OPENSSL_CONF"
  printf 'default_bits = 4096\n' >> "$OPENSSL_CONF"
  printf 'prompt = no\n' >> "$OPENSSL_CONF"
  printf 'default_md = sha256\n' >> "$OPENSSL_CONF"
  printf 'distinguished_name = dn\n' >> "$OPENSSL_CONF"
  printf 'x509_extensions = v3_req\n\n' >> "$OPENSSL_CONF"
  printf '[dn]\n' >> "$OPENSSL_CONF"
  printf 'CN = %s\n\n' "$DOMAIN" >> "$OPENSSL_CONF"
  printf '[v3_req]\n' >> "$OPENSSL_CONF"
  printf 'subjectAltName = @alt_names\n' >> "$OPENSSL_CONF"
  printf 'basicConstraints = CA:FALSE\n' >> "$OPENSSL_CONF"
  printf 'keyUsage = digitalSignature, keyEncipherment\n' >> "$OPENSSL_CONF"
  printf 'extendedKeyUsage = serverAuth\n\n' >> "$OPENSSL_CONF"
  printf '[alt_names]\n' >> "$OPENSSL_CONF"
  printf 'DNS.1 = %s\n' "$DOMAIN" >> "$OPENSSL_CONF"
  printf 'IP.1 = 127.0.0.1\n' >> "$OPENSSL_CONF"

  info "Generated OpenSSL config:"
  cat "$OPENSSL_CONF"

  openssl req -x509 -nodes \
    -newkey rsa:4096 \
    -keyout "$KEY_FILE" \
    -out    "$CERT_FILE" \
    -days   825 \
    -config "$OPENSSL_CONF"

  rm -f "$OPENSSL_CONF"

  # Generate DH params for improved forward secrecy (takes a moment)
  info "Generating DH parameters (2048-bit) — this may take a minute..."
  openssl dhparam -out "$DH_FILE" 2048

  chmod 600 "$KEY_FILE"
  chmod 644 "$CERT_FILE"

  info "Self-signed certificate generated:"
  info "  Certificate : $CERT_FILE"
  info "  Private Key : $KEY_FILE"
  info "  DH Params   : $DH_FILE"

  warn "Self-signed certificates will show a browser warning."
  warn "To trust it on Linux: sudo cp $CERT_FILE /usr/local/share/ca-certificates/${DOMAIN}.crt && sudo update-ca-certificates"
  warn "To trust it on macOS: sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain $CERT_FILE"
}

# --------------------------------------------------------------------------
# Mode: Let's Encrypt (Certbot)
# --------------------------------------------------------------------------
generate_letsencrypt() {
  require certbot

  [[ "$DOMAIN" == "localhost" ]] && error "Let's Encrypt does not work with 'localhost'. Provide a real domain."
  [[ -z "$EMAIL" ]]             && error "Email address is required for Let's Encrypt."

  info "Requesting Let's Encrypt certificate for: $DOMAIN (email: $EMAIL)"

  # Stop Nginx if running so Certbot's standalone server can bind port 80
  if docker ps --format '{{.Names}}' 2>/dev/null | grep -q nginx; then
    warn "Stopping Nginx container temporarily to free port 80..."
    docker compose -f "$PROJECT_ROOT/docker-compose.yml" stop nginx || true
    RESTART_NGINX=true
  fi

  certbot certonly \
    --standalone \
    --preferred-challenges http \
    --agree-tos \
    --no-eff-email \
    --email "$EMAIL" \
    -d "$DOMAIN"

  # Copy certs to our SSL directory
  mkdir -p "$SSL_DIR"
  CERT_SRC="/etc/letsencrypt/live/${DOMAIN}"

  cp -L "$CERT_SRC/fullchain.pem" "$SSL_DIR/cert.pem"
  cp -L "$CERT_SRC/privkey.pem"   "$SSL_DIR/key.pem"

  # DH params
  if [[ ! -f "$SSL_DIR/dhparam.pem" ]]; then
    info "Generating DH parameters..."
    openssl dhparam -out "$SSL_DIR/dhparam.pem" 2048
  fi

  chmod 600 "$SSL_DIR/key.pem"
  chmod 644 "$SSL_DIR/cert.pem"

  if [[ "${RESTART_NGINX:-false}" == "true" ]]; then
    info "Restarting Nginx..."
    docker compose -f "$PROJECT_ROOT/docker-compose.yml" start nginx || true
  fi

  # Auto-renewal cron (if not already set up)
  CRON_JOB="0 3 * * * certbot renew --quiet --deploy-hook 'cp -L /etc/letsencrypt/live/${DOMAIN}/fullchain.pem ${SSL_DIR}/cert.pem && cp -L /etc/letsencrypt/live/${DOMAIN}/privkey.pem ${SSL_DIR}/key.pem && docker compose -f ${PROJECT_ROOT}/docker-compose.yml exec nginx nginx -s reload'"
  if ! crontab -l 2>/dev/null | grep -q "certbot renew"; then
    (crontab -l 2>/dev/null; echo "$CRON_JOB") | crontab -
    info "Auto-renewal cron job installed."
  else
    info "Certbot renewal cron already exists, skipping."
  fi

  info "Let's Encrypt certificate installed:"
  info "  Certificate : $SSL_DIR/cert.pem"
  info "  Private Key : $SSL_DIR/key.pem"
}

# --------------------------------------------------------------------------
# Main
# --------------------------------------------------------------------------
echo ""
echo "=========================================="
echo "  SSL Certificate Generator"
echo "  Mode   : $MODE"
echo "  Domain : $DOMAIN"
echo "=========================================="
echo ""

case "$MODE" in
  selfsigned)   generate_selfsigned ;;
  letsencrypt)  generate_letsencrypt ;;
  *)            error "Unknown mode: '$MODE'. Use 'selfsigned' or 'letsencrypt'." ;;
esac

echo ""
info "Done. Update your .env file if needed and run: docker compose up -d"