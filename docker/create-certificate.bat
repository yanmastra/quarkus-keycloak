@echo off
setlocal

:: Load environment variables from docker_env.env using PowerShell
for /f "delims=" %%i in ('powershell -Command "Get-Content docker_env.env | Where-Object {$_ -notmatch '^#|^\s*$'} | ForEach-Object {$_}"') do set %%i

:: Check if keycloak/server.keystore exists
if exist keycloak\server.keystore (
    echo keycloak/server.keystore already exists!
) else (
    echo Running keytool to generate keystore for Keycloak...
    powershell -Command "& {
        $env:KEYCLOAK_KEYSTORE_PASSWORD='%KEYCLOAK_KEYSTORE_PASSWORD%'
        $env:KEYCLOAK_HOST='%KEYCLOAK_HOST%'
        Start-Process keytool -ArgumentList '-genkeypair -storepass \"$env:KEYCLOAK_KEYSTORE_PASSWORD\" -storetype PKCS12 -keyalg RSA -keysize 2048 -dname \"CN=$env:KEYCLOAK_HOST\" -alias \"$env:KEYCLOAK_HOST\" -ext \"SAN:c=DNS:$env:KEYCLOAK_HOST,IP:10.123.123.123\" -keystore ./keycloak/server.keystore' -Wait
    }"
)

:: Check if nginx certificates already exist
if exist nginx\certs\self-signed.crt if exist nginx\certs\self-signed.key (
    echo nginx/certs/self-signed already exists!
) else (
    mkdir nginx\certs
    echo Generating self-signed certificates for NGINX...
    powershell -Command "& {
        $env:KEYCLOAK_HOST='%KEYCLOAK_HOST%'
        Start-Process openssl -ArgumentList 'req -x509 -newkey rsa:4096 -keyout ./nginx/certs/self-signed.key -out ./nginx/certs/self-signed.crt -sha256 -days 365 -nodes -subj \"/C=ID/ST=Bali/L=Denpasar/O=IndonesianTechnologyCompany/OU=ITService/CN=$env:KEYCLOAK_HOST\"' -Wait
    }"
)

endlocal
