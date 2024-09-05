

```bash
$ sdk install java 21.0.4-tem
$ java -version
openjdk version "21.0.4" 2024-07-16 LTS
OpenJDK Runtime Environment Temurin-21.0.4+7 (build 21.0.4+7-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.4+7 (build 21.0.4+7-LTS, mixed mode)
```

```bash
$ sdk install java 23.1.4.r21-nik
$ sdk use java 23.1.4.r21-nik
$ java -version

openjdk version "21.0.4" 2024-07-16 LTS
OpenJDK Runtime Environment Liberica-NIK-23.1.4-1 (build 21.0.4+9-LTS)
OpenJDK 64-Bit Server VM Liberica-NIK-23.1.4-1 (build 21.0.4+9-LTS, mixed mode, sharing)
```

### GraalVM Component Updater v2.0.0

```bash
$ gu list

ComponentId              Version             Component name                Stability                     Origin
------------------------------------------------------------------------------------------------------------------------
graalvm                  23.1.4              GraalVM Core                  Experimental
native-image             23.1.4              Native Image                  Early adopter
```

More info: https://www.graalvm.org/22.2/reference-manual/graalvm-updater/

Think of GraalVM as a JDK with two modes.
- Just in time (JIT) compiling
- Ahead of time (AOT) compiling


### Building a Native Image using Native Build Tools

The following tries to build a native image but it fails on my machine (see error.txt)

```bash
$ mvn -Pnative native:compile -DskipTests
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




Resources:

- Getting started with Spring Boot AOT + GraalVM Native Images
https://www.youtube.com/watch?v=FjRBHKUP-NA
- 