Resources:

- Spring Tips: The Spring Authorization Server: https://www.youtube.com/watch?v=Yh8t04NG_K4
- Easy OAuth with the Durable Spring Authorization Server: https://www.youtube.com/watch?v=wq7oU2UCsbo
- What’s New In Spring Authorization Server 1.3 (SpringOne 2024): https://www.youtube.com/watch?v=V4ZrPBlG8EI
- Spring Authorization and Resource Servers in Separate Applications: https://www.youtube.com/watch?v=xEkzWRcxMl8
- https://www.youtube.com/watch?v=DaUGKnA7aro

Docs:

- https://spring.io/projects/spring-authorization-server


### Demo

- avoids problems with session cookie

```bash
sudo sh -c 'echo "127.0.0.1 authorization-server" >> /etc/hosts'
```
 
- Start `AuthorizationServerApplication (8080)` 
- Start `GreetingServiceApplication (8081)`
- Start `ClientApplication (8082)`

- Accessing the client application on `localhost:8082` the following happens:

```bash
GET http://localhost:8082 (302 Found) -> /oauth2/authorization/spring
GET http://localhost:8082/oauth2/authorizaton/spring (302 Found) -> http://authorization-server:8080/oauth2/authorize?response_type=code&client_id=client&scope=user.read%20openid&state=UHIfqiCc7GzNliLOBQJWGcBGoE0VUMksgr5IWJEBfXA%3D&redirect_uri=http://127.0.0.1:8082/login/oauth2/code/spring&nonce=F4Wn9YC8GwYRPKuMOPIBAmMwe4cJO7BVkWEnYJ4h6RE
GET http://authorization-server:8080/oauth2/authorize?response_type=code&client_id=client&scope=user.read%20openid&state=UHIfqiCc7GzNliLOBQJWGcBGoE0VUMksgr5IWJEBfXA%3D&redirect_uri=http://127.0.0.1:8082/login/oauth2/code/spring&nonce=F4Wn9YC8GwYRPKuMOPIBAmMwe4cJO7BVkWEnYJ4h6RE (302 Found) -> http://authorization-server:8080/login
GET http://authorization-server:8080/login (200 OK)
````

- After logging in the following happens:

```bash
POST http://authorization-server:8080/login 
  (302 Found) -> http://authorization-server:8080/oauth2/authorize?response_type=code&client_id=client&scope=user.read%20openid&state=fYwuLvRuh45T_MiwCsTxZJVd4_iAZ_9_i2_FYLbtADo%3D&redirect_uri=http://127.0.0.1:8082/login/oauth2/code/spring&nonce=-6o18XjJ_LRWBOWl-6trKmzUunVhBhSqwDHugXmQg6s&continue
  Set-Cookie JSESSIONID=4322F8E643501A37D184608139B4F69A; Path=/; HttpOnly

GET http://authorization-server:8080/oauth2/authorize?response_type=code&client_id=client&scope=user.read%20openid&state=fYwuLvRuh45T_MiwCsTxZJVd4_iAZ_9_i2_FYLbtADo%3D&redirect_uri=http://127.0.0.1:8082/login/oauth2/code/spring&nonce=-6o18XjJ_LRWBOWl-6trKmzUunVhBhSqwDHugXmQg6s&continue
  (200 OK)
```

- After submitting consent and agreeing on `user.read` scope the following happens:

```bash
POST http://authorization-server:8080/oauth2/authorize
  (302 Found) -> http://localhost:8082/login/oauth2/code/spring?code=sLvuFXsyWuUoTS8OOXRIFAuf95I4_KthcX6g0ZQRYjz9Ai1bFhgR2rNgsfBgCuJFXR2hSjH0Mt17rtCJo4n1M8xcVqtUiyKdbKI4fkIOYUwLOHQK_pOrOVjwbpoPub84&state=DtdGvsqbD3j7KmkIvTn3zJfL7K_DsEgm1JQFWXm5lWc%3D
GET http://localhost:8082/login/oauth2/code/spring?code=sLvuFXsyWuUoTS8OOXRIFAuf95I4_KthcX6g0ZQRYjz9Ai1bFhgR2rNgsfBgCuJFXR2hSjH0Mt17rtCJo4n1M8xcVqtUiyKdbKI4fkIOYUwLOHQK_pOrOVjwbpoPub84&state=DtdGvsqbD3j7KmkIvTn3zJfL7K_DsEgm1JQFWXm5lWc%3D
  (302 Found) -> /
GET http://localhost:8082  
```

### State

- Delete SESSION cookie on the localhost:8082
  - Inspect request in Network Tab - http://authorization-server:8080/oauth2/authorize is called
- Restart ClientApplication
  - Inspect request in Network Tab - http://authorization-server:8080/oauth2/authorize is called
- Restart only AuthorizationServer
  - normal localhost:8082 request, but later 401 failure 
- Restart ClientApplication & AuthorizationServer
  - Inspect request in Network Tab - you need to provide consent again 

### Repositories

- `WebSessionStore` - spring-web

- `ReactiveOAuth2AuthorizedClientService` - spring-security-oauth2-client
  - two types:
    - `InMemoryReactiveOAuth2AuthorizedClientService`
    - `R2dbcReactiveOAuth2AuthorizedClientService`

### Testing

- check accessing http://localhost:8082/ after restarting `ClientApplication`               
- check accessing http://localhost:8082/ without restarting `ClientApplication`


### Deploy to Kubernetes

```bash
$ cd authorization-server
$ mvn clean package spring-boot:build-image
$ k3d images import authorization-server:0.0.1-SNAPSHOT -c k3s-default
$ docker exec k3d-k3s-default-server-0 crictl images | grep authorization-server
$ kubectl apply -f k8s/k8s.yaml

$ cd client
$ mvn clean package spring-boot:build-image
$ k3d images import client:0.0.1-SNAPSHOT -c k3s-default
$ docker exec k3d-k3s-default-server-0 crictl images | grep client
$ kubectl apply -f k8s/k8s.yaml

$ cd greeting-service
$ mvn clean package spring-boot:build-image
$ k3d images import greeting-service:0.0.1-SNAPSHOT -c k3s-default
$ docker exec k3d-k3s-default-server-0 crictl images | grep greeting-service
$ kubectl apply -f k8s/k8s.yaml

$ kubectl port-forward deploy/authorization-server 8080:8080
$ kubectl port-forward deploy/client 8082:8080
$ kubectl port-forward deploy/greeting-service 8081:8080
```

### Kubernetes

```bash
$ k3d cluster create
# install redis
$ helm repo add bitnami https://charts.bitnami.com/bitnami
$ helm install my-redis bitnami/redis

# my-redis-master.default.svc.cluster.local for read/write operations (port 6379)
# my-redis-replicas.default.svc.cluster.local for read-only operations (port 6379)

# get redis passowrd
$ export REDIS_PASSWORD=$(kubectl get secret --namespace default my-redis -o jsonpath="{.data.redis-password}" | base64 -d)

# run a redis-client
$ kubectl run redis-client --restart='Never'  --env REDIS_PASSWORD=$REDIS_PASSWORD  --image registry-1.docker.io/bitnami/redis:latest --command -- sleep infinity
# attach to the client
$ kubectl exec -it redis-client -- bash
# connect to master
$ REDISCLI_AUTH="$REDIS_PASSWORD" redis-cli -h my-redis-master
# connect o replica
$ REDISCLI_AUTH="$REDIS_PASSWORD" redis-cli -h my-redis-replicas

$ info
$ role
```
