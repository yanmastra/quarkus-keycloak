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

This extension is published to GitHub Packages. See [Using Extensions as Dependencies](/README.md#using-extensions-as-dependencies) for setup instructions.

Add this to your `pom.xml`:
```xml
<dependency>
    <groupId>io.yanmastra</groupId>
    <artifactId>quarkus-microservices-common</artifactId>
    <version>4.0.4</version>
</dependency>
```

To verify the installation, run ``mvn clean install -DskipTests`` in the root folder of your project.

### Dependency usage
#### [`io.yanmastra.quarkus.microservices.common.entity.BaseEntity`](runtime/src/main/java/io/yanmastra/quarkus/microservices/common/entity/BaseEntity)
This is a base class for database entity that contains a couple of standard fields that need to be included to create an Entity
