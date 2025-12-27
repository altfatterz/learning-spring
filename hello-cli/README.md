### Hello CLI command line application

### Set your JDK

```bash
$ sdk install java 21.0.2-graalce
$ sdk use java 21.0.2-graalce
$ java -version
openjdk version "21.0.2" 2024-01-16
OpenJDK Runtime Environment GraalVM CE 21.0.2+13.1 (build 21.0.2+13-jvmci-23.1-b30)
OpenJDK 64-Bit Server VM GraalVM CE 21.0.2+13.1 (build 21.0.2+13-jvmci-23.1-b30, mixed mode, sharing)
```

### Run 

You can run it as a normal Spring Boot App.

### Build and Run

```bash
$ ./build.sh
$ ./target/hello-cli

shell>help

Built-In Commands
       help: Display help about available commands
       stacktrace: Display the full stacktrace of the last error.
       clear: Clear the shell screen.
       quit, exit: Exit the shell.
       history: Display or save the history of previously run commands
       version: Show version info
       script: Read and execute commands from a file.

Dad Joke Commands
       joke: 

Hello Commands
       goodbye: I will say goodbye
       hello: I will say hello
      
shell>hello
Hello World
       
       
shell>joke
I don’t play soccer because I enjoy the sport. I’m just doing it for kicks.       
```
