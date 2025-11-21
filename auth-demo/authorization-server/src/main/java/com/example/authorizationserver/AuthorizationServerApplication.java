package com.example.authorizationserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

@SpringBootApplication
public class AuthorizationServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthorizationServerApplication.class, args);
    }

    @Bean
    UserDetailsManager userDetailsManage() {
        var admin = User.builder().username("admin").password("{noop}admin").roles("admin", "user").build();
        var user = User.builder().username("user").password("{noop}user").roles("user").build();

        return new InMemoryUserDetailsManager(admin, user);
    }

}