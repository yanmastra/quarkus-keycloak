@echo off
setlocal enabledelayedexpansion

set "CURRENT_PATH=%cd%"
set "DEPENDENCIES=quarkus-base quarkus-authentication quarkus-microservices-common"

for %%d in (%DEPENDENCIES%) do (
    echo ------------------------------
    echo Installing Dependency %%d

    if exist "dependencies\%%d" (
        cd "dependencies\%%d"
    ) else (
        echo Folder "dependencies\%%d" does not exist
        exit /b 1
    )

    if "%%d"=="quarkus-base" (
        mvnw install:install-file -Dfile=runtime.jar -DpomFile=runtime.xml
        if errorlevel 1 exit /b 1
    ) else (
        mvnw clean install -N -DskipTests
        if errorlevel 1 exit /b 1

        mvnw install:install-file -Dfile=runtime.jar -DpomFile=runtime.xml
        if errorlevel 1 exit /b 1

        mvnw install:install-file -Dfile=deployment.jar -DpomFile=deployment.xml
        if errorlevel 1 exit /b 1
    )

    cd /d "%CURRENT_PATH%"
)
