### Spring Cloud Config Server Demo

1. Start `ConfigServerFileBackendApplication` in IntelliJ 

2. Verify that client configuration is exposed:

```bash
$ http :8888/config-server-client/dev/green
$ http :8888/config-server-client/dev/blue

$ http :8888/config-server-client/prd/green
$ http :8888/config-server-client/prd/blue 
```

3. Start the client in `dev` profile

```bash
$ mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

4. Verify that the configuration is taken from config server

```bash
$ http :8080/message
Hello Dev with 'file:' prefix!
```

5. Modify the property file and verify the config server knows about the change

```bash
$ http :8888/config-server-client/dev
...
    "propertySources": [
        {
            "name": "file:/Users/altfatterz/projects/learning-spring/config-server/client-configs/config-server-client-dev.yml",
            "source": {
                "message": "Hello Dev with 'file:' prefix! - changed"
            }
        }
    ],
```

6. Verify that the client does yet know about the change:

```bash
$ http :8080/message
Hello Dev with 'file:' prefix!
```

7. Send a refresh to the client

```bash
$ echo {} | http post :8080/actuator/refresh
```

In the logs you should see:

```bash
2024-09-20T09:23:21.259+02:00  INFO 35978 --- [config-server-client] [nio-8080-exec-8] o.s.c.c.c.ConfigServerConfigDataLoader   : Fetching config from server at : http://localhost:8888
2024-09-20T09:23:21.267+02:00  INFO 35978 --- [config-server-client] [nio-8080-exec-8] o.s.c.c.c.ConfigServerConfigDataLoader   : Located environment: name=config-server-client, profiles=[default], label=null, version=null, state=null
```

8. Verify that the client knows about the change:

```bash
$ http :8080/message
Hello Dev with 'file:' prefix! - changed
```
