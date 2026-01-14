## How to get dependencies package (.jar) 
These dependencies are not deployed to the maven central, so we need to compile them to the jar file and need manual installation to local maven.
There are some steps that we need to do: 
1. To compile dependency, just call ``./deploy-dependency.sh`` in root folder of this project
2. Jar and pom.xml file will be auto generated and placed in ``/deployed-dependencies``
3. To use them on your project, copy the dependency folder depend on which one that you need to use, and put them to the ``/dependencies`` folder in root folder of your project
4. Also copy this script ``/docker/install-jar.sh`` or ``/docker/install-jar.bat`` to root folder of your project. This file is to install the jar and pom file to the local Maven, so if someone need to run your project, they just need to run ``./install-jar.sh`` to install the dependency instead of cloning this project again, because the jar and pom already included to your project
5. Don't forget to add the dependency to your pom.xml project, see the detail in each dependency folder [``/dependencies/``](/dependencies)
6. To make use the dependency installed successfully, just run debug your Quarkus Project, if running well, the dependency installation is success