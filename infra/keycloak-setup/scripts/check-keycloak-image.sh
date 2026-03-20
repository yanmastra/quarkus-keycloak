#!/bin/bash
# Check what package manager and OS the Keycloak image provides
docker run --rm --entrypoint sh quay.io/keycloak/keycloak:24.0.4 -c '
echo "=== OS ==="
cat /etc/os-release | head -5

echo ""
echo "=== Package managers ==="
for cmd in rpm dnf microdnf yum apt-get apk; do
    if command -v $cmd &>/dev/null; then
        echo "FOUND: $cmd ($(command -v $cmd))"
    fi
done

echo ""
echo "=== Checking for envsubst / gettext ==="
command -v envsubst && echo "envsubst found" || echo "envsubst NOT found"

echo ""
echo "=== Checking for sed/awk (fallback) ==="
command -v sed && echo "sed found" || echo "sed NOT found"
command -v awk && echo "awk found" || echo "awk NOT found"

echo ""
echo "=== Shell available ==="
command -v bash && echo "bash found" || echo "bash NOT found"
command -v sh && echo "sh found" || echo "sh NOT found"
'
