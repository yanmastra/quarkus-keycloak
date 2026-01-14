If you want involved to develop the dependencies, you need to set up the project on your PC/Laptop
## How to se tup for dependency development in local environment?
Please follow these steps to run this project correctly
- [1. Create a project directory in your PC](#1-create-a-project-directory-in-your-pc)
- [2. Clone the Project](#2-clone-the-project)
- [3. Localhost Setup](#3-localhost-setup)
- [4. Environment Variables Setup](#4-environment-variables-setup)
- [5. Create Local DNS](#5-create-local-dns)
- [6. Create certificate for Keycloak and SLL](#6-create-certificate-for-keycloak-and-ssl)
- [7. Run Keycloak and PostgreSQL](#7-run-keycloak-and-postgresql)
- [8. Setup Keycloak](#8-setup-keycloak)

### 1. Create a project directory in your PC
~~This directory is to put all our projects that use dependencies from this repository.~~
This directory is to put all our projects, just to make them tidy

### 2. Clone the project
Enter to your new directory, and clone this repository. There is no special way to clone this project other than 
```shell
git clone https://github.com/yanmastra/quarkus-keycloak.git
``` 
or 
```shell
git clone git@github.com:yanmastra/quarkus-keycloak.git
```

### 2. Localhost setup
We need to set up our localhost or our Local Machine to make a DNS running on our Local Machine

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
> Coming soon

### 3. Environment Variables Setup
1. Please copy ``.env.example`` to ``.env``
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

### [7. Setup keycloak](/docs/keycloak-setup.md)
Click [the title](/docs/keycloak-setup.md) to see the complete instruction

### 8. Complete
Now, our setup to run this project is complete, so you can run [Rest Sample](/microservices/rest-sample) project by
open the terminal and enter to [microservices/rest-sample](/microservices/rest-sample) folder and then run ``./run-debug.sh`` script

[Back](../README.md#7-setup-keycloak)