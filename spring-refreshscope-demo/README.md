### RefreshScope demo


```bash
$ http :8080/route-config

HTTP/1.1 200
Connection: keep-alive
Content-Type: application/json
Date: Tue, 04 Mar 2025 15:35:23 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "db-accounts-route": false,
    "db-contract-route": false,
    "db-customers-routes": false
}
```

```bash
$ echo '{"value": true}' | http post :8080/actuator/routes-config/route-config.db-customers-route

HTTP/1.1 200
Connection: keep-alive
Content-Length: 65
Content-Type: application/json
Date: Tue, 04 Mar 2025 15:41:16 GMT
Keep-Alive: timeout=60

Property updated. Remember to call the actuator /actuator/refresh
```

```bash
$ http :8080/actuator/env

{
    "activeProfiles": [],
    "defaultProfiles": [
        "default"
    ],
    "propertySources": [
        {
            "name": "dynamicProperties",
            "properties": {
                "route-config.db-customers-route": {
                    "value": "true"
                }
            }
        },     
    ...
    
    
 }    
```


```bash
$ echo '{"route-config.db-contract-route": true}' | http post :8080/actuator/env 
```



### Refresh - empty refresh keys ???

```bash
$ http post :8080/actuator/refresh

HTTP/1.1 200
Connection: keep-alive
Content-Type: application/vnd.spring-boot.actuator.v3+json
Date: Tue, 04 Mar 2025 16:22:17 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

[]
```


```bash
$ http :8080/route-config

HTTP/1.1 200
Connection: keep-alive
Content-Type: application/json
Date: Tue, 04 Mar 2025 16:53:41 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "db-accounts-route": false,
    "db-contract-route": false,
    "db-customers-routes": true
}

```


TODO: test if spring-cloud-starter-config is needed or not? 