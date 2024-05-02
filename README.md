# Practicing to build Microservices with Quarkus, Quarkus-extension, Keycloak, and SSL integrated
This project was created to practice building Microservices with Quarkus, Securing services with Keycloak and testing the performance of hibernate-orm and hibernate-reactive.
In this project, there is a sample of the Rest-API project that contains many samples of Endpoint classes and samples of hibernate queries, etc.
If you are interested, let's see a couple of documentation below. 

<hr/>

## Table of contents
- [Introduction](#)
- [Requirement](#requirement-)
- [How to Setup ?](#how-to-setup)
  - [1. Clone the Project](#1-clone-the-project)
  - [2. Localhost Setup](#2-localhost-setup)
  - [3. Environment Variables Setup](#3-environment-variables-setup)
  - [4. Create Local DNS](#4-create-local-dns)
  - [5. Create certificate for Keycloak and SLL](#5-create-certificate-for-keycloak-and-ssl)
  - [6. Run Keycloak and PostgreSQL](#6-run-keycloak-and-postgresql)
  - [7. Setup Keycloak](#7-setup-keycloak)
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
   ```shell
   ...
   
   10.123.123.123 <domain name that you want>
   10.123.123.123 keycloak.<domain name that you want>
   ```
   it would be like 
    ```shell
   ...
   
   10.123.123.123 example.com 
   10.123.123.123 keycloak.example.com 
    ```
   or 
    ```shell
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
   ```shell
   ping 10.123.123.123
   ```
   and
   ```shell
   ping <your domain>
   ```
   if it works, the result would be like this
   ```text
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
7. Then we need to create two clients, one for backend and one for frontend, click 'Clients' on sidebar and click 'Create clients' button. 
   ![keycloak-add-client](/docs/img/keycloak-add-client.png) 
8. Fill the form like the image below and click "Next"<br/>
    ![keycloak-new-client](/docs/img/keycloak-new-client.png)
9. Check the following fields like the image bellow and click "Next" <br/>
    ![keycloak new client backend 2](/docs/img/keycloak-new-client-backend-2.png)
10. Then fill the "Valid Redirect URIs" field like image below and click "Save" <br/>
    ![keycloak valid redirect image setting](/docs/img/keycloak-new-client-backend-3.png)
11. "Backend" client is successfully created if you see this alert 
    ![success alert](/docs/img/keycloak-create-client-success.png)
12. Then back to the client list by click the "Clients" menu on sidebar, and you will see the new "backend" client there
    ![Backend client is successfully created](/docs/img/keycloak-new-backend-client-success.png)
13. Now we need to create one more client for frontend, 
    let's click "Create Client" once more
14. Then, fill the form like the image below, and then click "Next"
    ![Create new client for frontend](/docs/img/keycloak-new-client-frontend.png)
15. Don't change anything on this form, and then click "Next" 
    ![Create new client for frontend](/docs/img/keycloak-new-client-backend-2.png)
16. Fill the Valid redirect URIs like the image below and keep the other fields empty, and then click "Save" 
    ![Create valid redirect uris for client frontend](/docs/img/keycloak-new-client-backend-3.png)
17. "Frontend" client is successfully created, now we have two clients 
    ![Keycloak client list](/docs/img/keycloak-client-list.png)
18. Then we need to fill a-few more environment variables based on our Keycloak config, so edit the ``docker_env.env`` file 
    , find ``KEYCLOAK_REALM=``, and fill the value by your Realm name that you have been created! 
19. Back to Keycloak admin console, from the "Clients" list page, click the "backend", and then click "Credentials" tab, then 
    click "Copy to clipboard" icon on "Client Secret" field
    ![Copy Client Secret](/docs/img/keycloak-copy-client-secret.png) 
20. Then paste the value to ``KEYCLOAK_CLIENT_SECRET=`` on ``docker_env.env`` file, and then fill ``KEYCLOAK_CLIENT_ID=`` by ``backend`` 
21. The complete ``docker_env.env`` file would be like the code below ( don't worry about ``KAFKA_``, keep them by default )
```shell
SERVER_HOST=quarkus-practice.com
DATABASE_USERNAME=developer
DATABASE_PASSWORD=password1
DATABASE_HOST='127.0.0.1'
POSTGRES_EXTERNAL_PORT=25432

KEYCLOAK_EXTERNAL_PORT=28443
KEYCLOAK_ADMIN=quarkus-admin
KEYCLOAK_ADMIN_PASSWORD=password1
KEYCLOAK_KEYSTORE_PASSWORD=password1

KEYCLOAK_HOST=keycloak.quarkus-practice.com
KEYCLOAK_BASE_URL=https://keycloak.quarkus-practice.com
KEYCLOAK_REALM=quarkus-practice
KEYCLOAK_CLIENT_ID=backend
KEYCLOAK_CLIENT_SECRET=CsbE8MNrnQpYrZFICH0QwxnUbqFASRZd

KAFKA_EXTERNAL_PORT=39092
KAFKA_BOOTSTRAP_URL=PLAINTEXT://quarkus-practice.com:39092
KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092
KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://quarkus-practice.com:39092
```
22. To make us able to call any API on this project, we need create a user first, and the use it to log in to get an access token. 
    Please see the Keycloak documentation to create a user and login to keycloak by API

Now, our setup to run this project is complete, so you can run [Rest Sample](/microservices/rest-sample) project by 
open the terminal and enter to [microservices/rest-sample](/microservices/rest-sample) folder and then run ``./run-debug.sh`` script