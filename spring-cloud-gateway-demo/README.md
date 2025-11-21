
### Running Locally with 'local' profile

#### Matches the 'internal' route
```bash
$ http :8081/service/backend/api/v3/get\?name=robert

HTTP/1.1 200 OK
Content-Length: 13
Content-Type: text/plain;charset=UTF-8

Hello robert!
```

#### Matches the 'default' route

```bash
$ http :8081/service/backend/api/v3/get\?wrong-parameter=Zoltan

HTTP/1.1 200 OK
Access-Control-Allow-Credentials: true
Access-Control-Allow-Origin: *
Content-Length: 619
Content-Type: application/json
Date: Tue, 03 Dec 2024 14:09:11 GMT
Server: gunicorn/19.9.0

{
    "args": {
        "name": "zoltan"
    },
    "headers": {
        "Accept": "*/*",
        "Accept-Encoding": "gzip, deflate",
        "Content-Length": "0",
        "Forwarded": "proto=http;host=\"localhost:8081\";for=\"[0:0:0:0:0:0:0:1]:60919\"",
        "Host": "httpbin.org",
        "User-Agent": "HTTPie/3.2.3",
        "X-Amzn-Trace-Id": "Root=1-674f1107-186ba6e33da2d1566eae0b6d",
        "X-Forwarded-Host": "localhost:8081",
        "X-Forwarded-Prefix": "/service/backend/api/v3",
        "X-Request-Red": "blue"
    },
    "origin": "0:0:0:0:0:0:0:1, 178.238.175.138",
    "url": "http://localhost:8081/get?wrong-parameter=Zoltan"
}
```

#### Matches the 'default' route

```bash
$ http :8081/service/backend/api/v3/headers

HTTP/1.1 200 OK
Access-Control-Allow-Credentials: true
Access-Control-Allow-Origin: *
Content-Length: 458
Content-Type: application/json
Date: Thu, 29 Aug 2024 10:16:17 GMT
Server: gunicorn/19.9.0

{
    "headers": {
        "Accept": "*/*",
        "Accept-Encoding": "gzip, deflate",
        "Content-Length": "0",
        "Forwarded": "proto=http;host=\"localhost:8080\";for=\"[0:0:0:0:0:0:0:1]:62761\"",
        "Host": "httpbin.org",
        "User-Agent": "HTTPie/3.2.2",
        "X-Amzn-Trace-Id": "Root=1-66d04a71-71ba876b745b64094d56cb21",
        "X-Forwarded-Host": "localhost:8080",
        "X-Forwarded-Prefix": "/service/backend/api/v3",
        "X-Request-Red": "blue"
    }
}
```

### API KEY

```bash
$ http :8081/service/backend/api/v3/api-key 'x-api-key: simple-key'

HTTP/1.1 200 OK
Content-Length: 353
Content-Type: text/plain;charset=UTF-8

{Accept-Encoding=gzip, deflate, Accept=*/*, User-Agent=HTTPie/3.2.4, X-API-KEY=simple-key, Forwarded=proto=http;host="localhost:8081";for="[0:0:0:0:0:0:0:1]:63366", X-Forwarded-For=0:0:0:0:0:0:0:1, X-Forwarded-Proto=http, X-Forwarded-Prefix=/service/backend, X-Forwarded-Port=8081, X-Forwarded-Host=localhost:8081, host=localhost:8081, content-length=0}
```


### Enable Native Build

```bash
$ ./build.sh
./target/spring-cloud-gateway-demo

Started SpringCloudGatewayDemoApplication in 0.17 seconds (process running for 0.182)
```

