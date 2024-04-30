# Practicing to build Microservices with Quarkus, Quarkus-extension, Keycloak, and SSL integrated
This project was created to practice building Microservices with Quarkus, Securing services with Keycloak and testing the performance of hibernate-orm and hibernate-reactive.
In this project, there is a sample of the Rest-API project that contains many samples of Endpoint classes and samples of hibernate queries, etc.
If you are interested, let's see a couple of documentation below. 

<hr/>

## Table of contents
- [Requirement](#requirement-)
- [How to Setup ?](#how-to-setup--)
  - [1. Clone the Project](#1-clone-the-project)
  - [2. Localhost Setup](#2-localhost-setup-)
  - [3. Environment Variables Setup](#3-environment-variables-setup)
  - [4. Create Local DNS](#4-create-local-dns-)
  - [5. Create certificate for Keycloak and SLL](#5-create-certificate-for-keycloak-and-ssl)
  - [6. Run Keycloak and PostgreSQL](#6-run-keycloak-and-postgresql)
  - [7. Setup Keycloak](#7-setup-keycloak-)
- [How to run a service ?](/docs/how-to-run-a-services.md)
- [Postman Collection file](/docs/postman/)
- [Architecture Study Case](/docs/architecture-study-case.md)
- [Performance Test](/docs/peformance-test)

<hr/>

## Requirement 
1. GraalVM (Community Edition / Oracle) with Java 21
2. Docker

> :warning: It would be better you install ``sdkman`` on your computer, and then use ``sdkman`` to install GraalVM 
> <br/> [See this to install ``SDKMAN`` ](https://sdkman.io/install) 

<hr/>

## How to setup ? 
Please follow these steps to run this project correctly
### 1. Clone the project
There is no special way to clone this project

### 2. Localhost setup 
We need to setup our localhost or our Local Machine to make a DNS running on our Local Machine

#### - Linux / MacOS 
1. Open your terminal
2. Type ``sudo vi /etc/hosts`` and input your computer password
3. Press i on keyboard and type this on new line 
   ```
   ...
   
   10.123.123.123 <domain name that you want>
   10.123.123.123 keycloak.<domain name that you want>
   ```
   it would be like 
    ``` 
   ...
   
   10.123.123.123 example.com 
   10.123.123.123 keycloak.example.com 
    ```
   or 
    ``` 
   ...
   
   10.123.123.123 practicing-quarkus.com
   10.123.123.123 keycloak.practicing-quarkus.com 
    ```
   or something else
4. Press ``Esc`` on keyboard, then press ``Shift`` + ``Z`` twice

#### - Windows 
- Coming soon

### 3. Environment Variables Setup
1. Please copy ``docker_env.env.example`` to ``docker_env.env`` 
2. Fill in these variable first
   - ``SERVER_HOST`` is the domain name that you have added before to ``/etc/hosts`` 
   - ``KEYCLOAK_HOST`` is your domain name with prefix ``keycoak.``
   - All environment variables with prefix ``DATABASE_``
   - ``KEYCLOAK_EXTERNAL_PORT`` is accessible port of keycloak container  
   - ``KEYCLOAK_ADMIN`` and ``KEYCLOAK_ADMIN_PASSWORD`` is a user credential will be used to sign in to keycloak admin console
   - ``KEYCLOAK_KEYSTORE_PASSWORD`` keystore password to secure the keycloak

### 4. Create local DNS 
This step is to make our domain works on our local
1. Open your terminal
2. Navigate to this project root folder
3. Type ``./create-local-dns.sh`` and enter
4. The result would be like this <br/>
![image](/docs/img/create-local-dns.png)
5. To make sure your domain works, type this on terminal, 
   ```
   ping 10.123.123.123
   ```
   and
   ```
   ping <your domain>
   ```
   if it works, the result would be like this
   ```
   PING q-learning.com (10.123.123.123): 56 data bytes
   64 bytes from 10.123.123.123: icmp_seq=0 ttl=64 time=0.118 ms
   64 bytes from 10.123.123.123: icmp_seq=1 ttl=64 time=0.299 ms
   64 bytes from 10.123.123.123: icmp_seq=2 ttl=64 time=0.296 ms
   64 bytes from 10.123.123.123: icmp_seq=3 ttl=64 time=0.220 ms
   ```

### 5. Create certificate for keycloak and SSL
Certificate is needed to secure our keycloak
1. Run ``./create-certificate.sh`` on your terminal 
2. After that check on folder ``./nginx/certs``, ``self-signed.crt`` and ``self-signed.key`` files would be there <br/>
   ![image](/docs/img/nginx-certs.png)
3. And then check on folder ``./docker/keycloak/``, ``server.keystore`` would be there
   ![image](/docs/img/server-keystore.png) <br/>
   (don't worry about Dockerfile, it would be generated letter)

### 6. Run Keycloak and PostgreSQL
1. On your terminal, please run this ``./compose-up-keycloak-posgress.sh`` 
2. Please wait until finish, the result would be like this 
   ![image](/docs/img/compose-up-keycloak-postgress.png) 

### 7. Setup keycloak 
1. Open https://keycloak.<your domain\> on your browser 
2. Please sign in by username and password that you set before on these variables: ``KEYCLOAK_ADMIN`` and ``KEYCLOAK_ADMIN_PASSWORD``
3. If sign in success, you will be navigated to Keycloak Admin Console page, the first thing we need to do on Keycloak Admin Console is creating a new Realm for our project, 
so click the dropdown on the top right of screen, and select 'Create realm' button. 
   ![image](/docs/img/keycloak-select-realm.png)
4. On the realm name field, please input the realm name that you want, example: quarkus-learning, q-learning, practicing-quarkus, or something else, 
Realm name should be without space, and better if all characters are lowercase
   ![image](/docs/img/keycloak-new-realm.png)
5. Click Create
6. If the realm successfully created, the page should be navigated to new Realm like this. 
   ![new-realm-dashboard](/docs/img/new-realm-dashboard.png)
7. Then we need to create new client, click 'Clients' on sidebar and click 'Create clients' button. 
   ![keycloak-add-client](/docs/img/keycloak-add-client.png) 
8. Fill the form like the image below and click "Next"<br/>
     ![keycloak-new-client](/docs/img/keycloak-new-client.png)
9. Check the following fields like the image bellow and click "Next" <br/>
    ![keycloak new client backend 2](/docs/img/keycloak-new-client-backend-2.png)