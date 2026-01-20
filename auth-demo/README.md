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

### Repositories

- `WebSessionStore` - spring-web

 
- `ReactiveOAuth2AuthorizedClientService` - spring-security-oauth2-client
  - two types:
    - `InMemoryReactiveOAuth2AuthorizedClientService`
    - `R2dbcReactiveOAuth2AuthorizedClientService`


### Testing

- check accessing http://localhost:8082/ after restarting `ClientApplication`               
- check accessing http://localhost:8082/ without restarting `ClientApplication`
