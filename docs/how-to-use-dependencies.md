## How to use extensions

These extensions are published to **GitHub Packages**. You can use them in your own Quarkus project without cloning this repository.

For full setup instructions (creating a PAT, configuring Maven, adding the repository and dependencies), see the [Using Extensions as Dependencies](/README.md#using-extensions-as-dependencies) section in the main README.

### Alternative: Local installation

If you prefer to build and install locally instead of using GitHub Packages:

1. Clone this repository
2. Run ``./scripts/deploy-dependency.sh`` from the root folder to compile all extensions
3. The JARs and POM files will be generated in ``/deployed-dependencies``
4. Each extension is also installed to your local Maven repository (`~/.m2`) via `mvn install`
5. Add the dependency to your project's `pom.xml` (see the main README for artifact coordinates)