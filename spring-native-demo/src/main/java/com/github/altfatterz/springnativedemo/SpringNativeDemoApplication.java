package com.github.altfatterz.springnativedemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
// @RegisterReflectionForBinding(InternalCustomer.class)
@ImportRuntimeHints(RuntimeHints.class)
public class SpringNativeDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringNativeDemoApplication.class, args);
    }


}

