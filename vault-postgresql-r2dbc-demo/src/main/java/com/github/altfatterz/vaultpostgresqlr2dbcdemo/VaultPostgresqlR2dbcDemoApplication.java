package com.github.altfatterz.vaultpostgresqlr2dbcdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@SpringBootApplication
@EnableConfigurationProperties(DatabaseConnectionProperties.class)
public class VaultPostgresqlR2dbcDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(VaultPostgresqlR2dbcDemoApplication.class, args);
    }

}
