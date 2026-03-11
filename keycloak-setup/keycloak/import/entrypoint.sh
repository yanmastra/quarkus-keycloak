#!/bin/bash
# =============================================================================
# Keycloak Entrypoint Script
# Location inside container: /opt/keycloak/data/import-templates/entrypoint.sh
#
# Logic:
#   - On FIRST RUN (sentinel file absent): start Keycloak with --import-realm
#     This imports the realm JSON file automatically.
#   - On SUBSEQUENT RUNS (sentinel file present): start normally without import
#     This ensures the realm is never re-imported, preserving all changes made
#     through the admin UI after the first run.
#
# The sentinel file is stored in the persistent volume so it survives restarts.
# =============================================================================

set -e

SENTINEL_FILE="/opt/keycloak/data/.realm-imported"
TEMPLATE_DIR="/opt/keycloak/data/import-templates"
IMPORT_DIR="/opt/keycloak/data/import"
REALM_FILE="${REALM_IMPORT_FILE:-realm-export}.json"

echo "=========================================="
echo "  Keycloak Entrypoint"
echo "  Realm import file: ${REALM_FILE}"
echo "=========================================="

# ---------------------------------------------------------------------------
# Process realm template — replace ${VAR} placeholders with env-var values.
# The template lives in the read-only mount (import-templates/); the rendered
# file is written to the writable import/ dir that Keycloak's --import-realm
# reads from.
# Uses sed instead of envsubst (not available in the Keycloak UBI image).
# Only the variables listed below are substituted — Keycloak's own
# placeholders like ${client_id} are left untouched.
# ---------------------------------------------------------------------------
if [ -f "$TEMPLATE_DIR/$REALM_FILE" ]; then
    echo "[INFO] Processing realm template with sed..."
    mkdir -p "$IMPORT_DIR"
    sed \
        -e "s|\${DOMAIN}|${DOMAIN}|g" \
        -e "s|\${KC_REALM_NAME}|${KC_REALM_NAME}|g" \
        -e "s|\${KC_REALM_DISPLAY_NAME}|${KC_REALM_DISPLAY_NAME}|g" \
        -e "s|\${KC_WEB_APP_REDIRECT_URI}|${KC_WEB_APP_REDIRECT_URI}|g" \
        -e "s|\${KC_BACKEND_CLIENT_SECRET}|${KC_BACKEND_CLIENT_SECRET}|g" \
        -e "s|\${KC_SMTP_FROM}|${KC_SMTP_FROM}|g" \
        "$TEMPLATE_DIR/$REALM_FILE" > "$IMPORT_DIR/$REALM_FILE"
    echo "[INFO] Rendered $IMPORT_DIR/$REALM_FILE"
else
    echo "[WARN] Template not found at $TEMPLATE_DIR/$REALM_FILE"
fi

if [ ! -f "$SENTINEL_FILE" ]; then
    echo "[FIRST RUN] Sentinel not found — performing realm import..."

    if [ ! -f "$IMPORT_DIR/$REALM_FILE" ]; then
        echo "[WARN] Realm file not found at $IMPORT_DIR/$REALM_FILE — skipping import."
        echo "[INFO] Starting Keycloak without realm import..."
        exec /opt/keycloak/bin/kc.sh start --optimized
    fi

    echo "[INFO] Starting Keycloak with realm import..."
    /opt/keycloak/bin/kc.sh start \
        --optimized \
        --import-realm \
        &

    KC_PID=$!

    # Wait for Keycloak to be healthy before marking import complete
    echo "[INFO] Waiting for Keycloak to become healthy..."
    MAX_WAIT=120
    ELAPSED=0
    until bash -c 'cat < /dev/null > /dev/tcp/localhost/8080' 2>/dev/null; do
        sleep 5
        ELAPSED=$((ELAPSED + 5))
        if [ $ELAPSED -ge $MAX_WAIT ]; then
            echo "[ERROR] Keycloak did not become healthy within ${MAX_WAIT}s."
            kill $KC_PID 2>/dev/null
            exit 1
        fi
        echo "[INFO] Still waiting... (${ELAPSED}s elapsed)"
    done

    echo "[SUCCESS] Keycloak is healthy. Marking realm import as complete."
    touch "$SENTINEL_FILE"
    echo "Realm imported at: $(date -u)" >> "$SENTINEL_FILE"

    # Wait on the Keycloak process so the container doesn't exit
    wait $KC_PID

else
    echo "[SUBSEQUENT RUN] Sentinel found — skipping realm import."
    cat "$SENTINEL_FILE"
    echo "[INFO] Starting Keycloak normally..."
    exec /opt/keycloak/bin/kc.sh start --optimized
fi