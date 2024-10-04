## Introduction
This project is build by Quarkus, you can see what is Quarkus on their [official website](https://quarkus.io).
Goals of this project is <strong>"to learn and find a better way how to build a Microservices System Architecture using Quarkus"</strong>,
so the description on the ``README.md`` file will be changed periodically following updates of this project. <br/>
There are some important things you should know about this project
1. Root Folder description
    - [`dependencies`](/dependencies) contains a couple of "Quarkus-extensions" and dependency projects will be used on each service.
    - [`docker`](/docker) contains files that needed to build Container of Keycloak, PostgresQL, nginx, etc
    - [`docs`](/docs) contains image assets and markdown (`.md`) files of project documentation
    - [`microservices`](/microservices) is a parent project of all Microservices project,
2. Incomplete parts
    1. [``microservices/rest-sample``](/microservices/rest-sample) is not completed because need to add some more sample there.
    2. Performance test with JMeter is totally not created yet.
       This part will test some API endpoint from Rest Sample and Rest-Sample Reactive services to compare their performance
    3. Real microservices sample, it will consist of several examples of backend services needed to build an e-commerce application
       This part will use Kafka for message broker.
    4. Documentation of the Architecture
3. This project is developed and tested on MacOS, it might not be working properly on Windows OS

[Back](../README.md)