# Practicing to build Quarkus, Quarkus-extension, Keycloak, and SSL integrated

## Requirement 
1. GraalVM (Community Edition / Oracle) with Java 21
2. Docker
> It would be better you install ``sdkman`` on your computer, and then use ``sdkman`` to install GraalVM 
> [See this to install](https://sdkman.io/install) 

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
   10.123.123.123 <domain name that you want>
   10.123.123.123 keycloak.<domain name that you want>
   ```
   it would be like 
    ``` 
   10.123.123.123 example.com 
   10.123.123.123 keycloak.example.com 
    ```
   or 
    ``` 
   10.123.123.123 practicing-quarkus.com
   10.123.123.123 keycloak.practicing-quarkus.com 
    ```
   or something else
4. Press ``Esc`` on keyboard, then press ``Shift`` + ``Z`` twice

#### - Windows 
- Coming soon

### 3. Environment Setup
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
4. The result would be like this 
   ![image](/docs/img/create-local-dns.png)
5. To make sure your domain works, type this on terminal, 
   ```
   ping 10.123.123.123
   ```
   or
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
2. After that check on folder ``./nginx/certs``, ``self-signed.crt`` and ``self-signed.key`` files would be there
   ![image](/docs/img/nginx-certs.png)
3. And then check on folder ``./docker/keycloak/``, ``server.keystore`` would be there
   ![image](/docs/img/server-keystore.png)

### 6. Run Keycloak and PostgreSQL
1. On your terminal, please run this ``./compose-up-keycloak-posgress.sh`` 
2. Please wait until finish, 