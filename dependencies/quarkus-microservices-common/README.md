# Microservices Common
This is a quarkus extension to help Backend developer life easier. Contains a lot of helper to create basic endpoint for CRUD. 
If you got interesting, lets explore

## Documentation
### Dependencies
- quarkus-arc
- quarkus-rest-jackson
- quarkus-jsonb
- commons-lang3:3.18.0
- quarkus-hibernate-orm-panache
- quarkus-jdbc-postgresql

### Installation
1. Come to root folder of this project
1. Run [``./deploy-dependency.sh``](/deploy-dependency.sh)
1. Come to [``/deployed-dependencies``](/deployed-dependencies)
1. Copy folder [``quarkus-microservices-common``](/deplyed-dependencies/quarkus-microservices-common) and put to folder [``/dependencies``]() on root folder of your project
1. Add this to your pom.xml project in dependencies part 
    ```xml
    <dependency>
        <groupId>io.yanmastra</groupId>
        <artifactId>quarkus-microservices-common</artifactId>
        <version>4.0.1</version>
    </dependency>
    ```
1. Try to run ``mvn clean install -DskipTests`` in root folder of your project, if there is no error, the dependency is installed successfully

### Dependency usage
#### [`io.yanmastra.quarkus.microservices.common.entity.BaseEntity`](runtime/src/main/java/io/yanmastra/quarkus/microservices/common/entity/BaseEntity)
This is a base class for database entity that contains a couple of standard fields that need to be included to create an Entity
