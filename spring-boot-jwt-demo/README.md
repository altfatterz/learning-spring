# Spring Boot JWT Demo

## Auth0 Dashboard

- Create an API Application, named `spring-boot-jwt-demo`, audience set to `spring-boot-jwt-demo`

## Start the application and in the logs you will see

```bash
HTTP GET https://altfatterz.us.auth0.com/.well-known/jwks.json
```
The content is used by the Spring Boot app to verify the signature of the JWT token. 

## Create an access token (JWT) 

In `Auth0` dashboard for the previously created application `spring-boot-jwt-demo` you can ask to create a JWT

```bash
$ curl --request POST \
  --url https://altfatterz.us.auth0.com/oauth/token \
  --header 'content-type: application/json' \
  --data '{
    "client_id":"<CLIENT_ID>",
    "client_secret":"<CLIENT_SECRET>",
    "audience":"spring-boot-jwt-demo",
    "grant_type":"client_credentials"
  }'

# you will get a response like:
# the created token will expire after 24 hours by default  
{
  "access_token": "<ACCESS_TOKEN>",
  "token_type": "Bearer"
}  

```

## Test the application

```bash
# save the returned access token to a variable
$ export TOKEN=<ACCESS_TOKEN>

# view the token
$ jwt decode $TOKEN

# does not need authentication
$ curl http://localhost:8080/public
This is a public endpoint. No JWT needed

# needs authentication
$ curl -v http://localhost:8080/private
< HTTP/1.1 401
< WWW-Authenticate: Bearer resource_metadata="http://localhost:8080/.well-known/oauth-protected-resource"

# send the $TOKEN 
$ curl -v http://localhost:8080/private --header "Authorization: Bearer $TOKEN"
Hello, MvZAiiEl5DJzDjvily8avtD2GWAL0eS6@clients! You have a valid JWT

# change the audience and send the token again you will see error message:
$ curl -v http://localhost:8080/private --header "Authorization: Bearer $TOKEN"
< HTTP/1.1 401
< WWW-Authenticate: Bearer error="invalid_token", 
error_description="An error occurred while attempting to decode the Jwt: The required audience is missing", 
error_uri="https://tools.ietf.org/html/rfc6750#section-3.1", 
resource_metadata="http://localhost:8080/.well-known/oauth-protected-resource"
```

