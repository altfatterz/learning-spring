package com.github.altfatterz.vaultpostgresqlr2dbcdemo;

import com.github.altfatterz.vaultpostgresqlr2dbcdemo.config.DBConnectionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DBConnectionProperties.class)
public class VaultPostgresqlR2dbcDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(VaultPostgresqlR2dbcDemoApplication.class, args);
    }

}
