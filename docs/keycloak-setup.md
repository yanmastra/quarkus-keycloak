### 7. Setup Keycloak 
Please follow these steps to setup the Keycloak correctly

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
    ![Create new client for frontend](/docs/img/keycloak-new-client-frontend-2.png)
16. Fill the Valid redirect URIs like the image below and keep the other fields empty, and then click "Save"
    ![Create valid redirect uris for client frontend](/docs/img/keycloak-new-client-frontend-3.png)
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

[Back](../README.md#7-setup-keycloak) 