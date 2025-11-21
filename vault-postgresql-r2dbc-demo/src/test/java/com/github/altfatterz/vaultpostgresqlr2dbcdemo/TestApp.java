package com.github.altfatterz.vaultpostgresqlr2dbcdemo;

import org.springframework.boot.SpringApplication;

public class TestApp {

    public static void main(String[] args) {
        SpringApplication.from(VaultPostgresqlR2dbcDemoApplication::main)
                .with(TestAppConfig.class).run(args);
    }
}
