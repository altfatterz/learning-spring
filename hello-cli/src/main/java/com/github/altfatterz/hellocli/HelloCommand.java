package com.github.altfatterz.hellocli;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class HelloCommand {

    @ShellMethod(key = "hello", value = "This will echo 'Hello World' to the console")
    public String sayHello() {
        return "Hello World";
    }
}
