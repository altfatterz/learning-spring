package com.github.altfatterz.vaultdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

// Spring Cloud Vault uses VaultOperations to interact with Vault.
// Properties from Vault get mapped to MyConfiguration for type-safe access.
// @EnableConfigurationProperties(MyConfiguration.class) enables configuration property mapping and registers a MyConfiguration bean.

@SpringBootApplication
@EnableConfigurationProperties(MyConfiguration.class)
public class VaultDemoApplication implements CommandLineRunner {

    private final MyConfiguration configuration;

    public VaultDemoApplication(MyConfiguration configuration) {
        this.configuration = configuration;
    }

    public static void main(String[] args) {
        SpringApplication.run(VaultDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Logger logger = LoggerFactory.getLogger(VaultDemoApplication.class);

        logger.info("----------------------------------------");
        logger.info("Configuration properties");
        logger.info("   example.username is {}", configuration.getUsername());
        logger.info("   example.password is {}", configuration.getPassword());
        logger.info("----------------------------------------");
    }

}
