### Spring Native Demo

More info: https://www.graalvm.org/22.2/reference-manual/graalvm-updater/

Think of `GraalVM` as a JDK with two modes.
- Just in time (JIT) compiling
- Ahead of time (AOT) compiling


### Issue with native build:

- reflection
- resource loading
- serialization
- proxy creation

Did not work with this one `23.1.4.r21-nik` I get ArrayIndexOutOfBoundsException see error.txt

```bash
$ sdk install java 23.1.4.r21-nik
$ sdk use java 23.1.4.r21-nik
$ java -version
openjdk version "21.0.4" 2024-07-16 LTS
OpenJDK Runtime Environment Liberica-NIK-23.1.4-1 (build 21.0.4+9-LTS)
OpenJDK 64-Bit Server VM Liberica-NIK-23.1.4-1 (build 21.0.4+9-LTS, mixed mode, sharing)
```

I could compile natively on Mac with this JDK `21.0.2-graalce`

```bash
$ sdk install java 21.0.2-graalce
$ sdk use java 21.0.2-graalce
$ java -version
openjdk version "21.0.2" 2024-01-16
OpenJDK Runtime Environment GraalVM CE 21.0.2+13.1 (build 21.0.2+13-jvmci-23.1-b30)
OpenJDK 64-Bit Server VM GraalVM CE 21.0.2+13.1 (build 21.0.2+13-jvmci-23.1-b30, mixed mode, sharing)
```

### Building a Native Image using Native Build Tools

The following tries to build a native image but it fails on my machine (see error.txt)

```bash
$ mvn -Pnative clean native:compile
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:59 min
```

- During build it downloads the `GraalVM` Reachability Metadata repository into the `target` folder.
  https://github.com/oracle/graalvm-reachability-metadata

- Run the native build

```
./target/spring-navtive-demo

Starting AOT-processed SpringNativeDemoApplication using Java 21.0.2 with PID 17512
...
Started SpringNativeDemoApplication in 0.127 seconds (process running for 0.139)
```

- `Memory usage`:

```bash
$ vmmap 17512 | grep Physical
Physical footprint:         43.5M
Physical footprint (peak):  43.5M
```

- `Size on disk`

```bash
$ ls -lh target/spring-native-demo | awk '{ print $5; }'
83M
```
- target/spring-aot

Two generated files: `reflect-config.json` , `resource-config.json`

Check the `reflect-config.json`: 

```bash
{
    "name": "com.github.altfatterz.springnativedemo.Customer",
    "allDeclaredFields": true,
    "allDeclaredConstructors": true,
    "methods": [
      {
        "name": "getFirstName",
        "parameterTypes": [ ]
      },
      {
        "name": "getLastName",
        "parameterTypes": [ ]
      }
    ]
  }
```

### Building a Native Image Using Buildpacks

```bash
$ mvn -Pnative spring-boot:build-image
...
Pulling builder image 'docker.io/paketobuildpacks/builder-jammy-tiny:latest' 100%
Pulling run image 'docker.io/paketobuildpacks/run-jammy-tiny:latest' 100%

NFO]     [creator]     6 of 15 buildpacks participating
[INFO]     [creator]     paketo-buildpacks/ca-certificates   3.8.4
[INFO]     [creator]     paketo-buildpacks/bellsoft-liberica 10.8.2
[INFO]     [creator]     paketo-buildpacks/syft              1.47.2
[INFO]     [creator]     paketo-buildpacks/executable-jar    6.11.0
[INFO]     [creator]     paketo-buildpacks/spring-boot       5.31.0
[INFO]     [creator]     paketo-buildpacks/native-image      5.14.2
```

```bash
$ docker images | grep docker spring-native-demo
spring-native-demo                          0.0.1-SNAPSHOT        4b34d972cded   44 years ago    118MB
```

Run the application

```bash
$ docker run -p 8080:8080 docker.io/library/spring-native-demo:0.0.1-SNAPSHOT

Starting AOT-processed SpringNativeDemoApplication using Java 21.0.3 with PID 1
```

```bash
$ http :8080/actuator/health
{
    "status": "UP"
}
```


### GraalVM Reachability Metadata

https://www.graalvm.org/latest/reference-manual/native-image/metadata/

https://github.com/oracle/graalvm-reachability-metadata

This repository enables users of GraalVM Native Image to share and reuse metadata for libraries and frameworks in the Java ecosystem.


### AOT Compilation

Resources:

- Dan Vega: https://www.youtube.com/watch?v=FjRBHKUP-NA
- Joris Kuipers: https://www.youtube.com/watch?v=HThlVyustWo&t=272s
- Bootiful GraalVM by Josh Long & Alina Yurenko: https://www.youtube.com/watch?v=3OBhk1c0GBs