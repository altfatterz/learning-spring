
### Running Locally

```bash
$ http :8081/service/backend/api/v3/get\?name=Zoltan

HTTP/1.1 200 OK
Content-Length: 13
Content-Type: text/plain;charset=UTF-8

Hello Zoltan!
```

```bash
$ http :8081/service/backend/api/v3/get

HTTP/1.1 200 OK
Content-Length: 13
Content-Type: text/plain;charset=UTF-8

Hello World!
```

```bash
$ http :8080/service/backend/api/v3/headers

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

### Enable Native Build

```bash
$ ./build.sh
./target/spring-cloud-gateway-demo

Started SpringCloudGatewayDemoApplication in 0.17 seconds (process running for 0.182)
```

