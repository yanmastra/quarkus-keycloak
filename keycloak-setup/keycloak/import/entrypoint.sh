#!/bin/bash
# =============================================================================
# Keycloak Entrypoint Script
# Location inside container: /opt/keycloak/data/import/entrypoint.sh
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
IMPORT_DIR="/opt/keycloak/data/import"
REALM_FILE="${REALM_IMPORT_FILE:-realm-export}.json"

echo "=========================================="
echo "  Keycloak Entrypoint"
echo "  Realm import file: ${REALM_FILE}"
echo "=========================================="

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
    until curl -sf http://localhost:8080/health/ready > /dev/null 2>&1 || \
          curl -sf http://localhost:8080/auth/health/ready > /dev/null 2>&1; do
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