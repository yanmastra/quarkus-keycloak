## Introduction
This project is build by Quarkus, you can see what is Quarkus on their [official website](https://quarkus.io).
Goals of this project is <strong>"to learn and find a better way how to build a Microservices System Architecture using Quarkus"</strong>,
so the description on the ``README.md`` file will be changed periodically following updates of this project. <br/>
There are some important things you should know about this project
1. Folder Structure
    - [`dependencies`](/dependencies) contains a couple of "Quarkus-extensions" and dependencies that can be used on backend service. Here are the list of sort intro of what they are: 
        - [quarkus-base](/dependencies/quarkus-base) is a <strong>simple dependency</strong> that contains base classes that used in all dependencies here
        - [quarkus-authentication](/dependencies/quarkus-authentication) is a <strong>quarkus extension</strong> contains some features e.g. generator of encrypted JWT, bean to validate (authorize) the http request using Authorization: Bearer <token> or Cookies token, Error mapper, and Logging filter. This extension can't be combined with [quarkus-authorization](/dependencies/quarkus-authorization) due to conflict feature.
        - [quarkus-authorisation](/dependencies/quarkus-authorization) is a <strong>quarkus extension</strong> typically used for a Backend that integrated with <strong>Keycloak</strong>. The feature is provide a bean to validate (authorize) the http request using Authorization: Bearer <token> if the token come from Keycloak
        - [quarkus-microservices-common](/dependencies/quarkus-microservices-common) is a <strong>quarkus extension</strong> contains some classes and beans to make us easier to create CRUD Resource. Typically used for Backend service, very useful for creating Master Data API, endpoint to support pagination, data filter and sorting. Features: 
          - Auto convert request param to HQL query to retrieve the data with filter and sort, it's like a GraphQL but simpler, 
          - Auto provide basic endpoint for CRUD process, typically 5 Endpoint, e.g 1. API to get a Paginated List of data with filtering and sorting, 1. API to get a single detailed object, 1. API to create, 1. API to update, and 1. to delete
    - [`docker`](/docker) contains files that needed to build Container of Keycloak, PostgresQL, nginx, etc
    - [`docs`](/docs) contains image assets and markdown (`.md`) files of project documentation
    - [`microservices`](/microservices) is a parent project of all Microservices project,
2. Incomplete Parts
    1. [``microservices/rest-sample``](/microservices/rest-sample) is not completed because need to add some more sample there.
    2. Performance test with JMeter is totally not created yet.
       This part will test some API endpoint from Rest Sample and Rest-Sample Reactive services to compare their performance
    3. Real microservices sample, it will consist of several examples of backend services needed to build an e-commerce application
       This part will use Kafka for message broker.
    4. Documentation of the Architecture
3. This project is developed and tested on MacOS, it might not be working properly on Windows OS

[Back](../README.md)