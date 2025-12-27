package com.github.altfatterz.hellocli.commands;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class HelloCommands {

    @ShellMethod(key = "hello", value = "I will say hello")
    public String sayHello(@ShellOption(defaultValue = "World") String arg) {
        return "Hello, "  + arg + "!";
    }

    @ShellMethod(key = "goodbye", value = "I will say goodbye")
    public String sayGoodbye() {
        return "Goodbye!";
    }

}
