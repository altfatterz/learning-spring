package com.github.altfatterz.vaultpostgresqlr2dbcdemo;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;

@Configuration
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    private DatabaseConnectionProperties databaseConnectionProperties;

    public DatabaseConfig(DatabaseConnectionProperties databaseConnectionProperties) {
        this.databaseConnectionProperties = databaseConnectionProperties;
    }

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(databaseConnectionProperties.getHost())
                        .database(databaseConnectionProperties.getDatabase())
                        .port(databaseConnectionProperties.getPort())
                        .username(databaseConnectionProperties.getUsername())
                        .password(databaseConnectionProperties.getPassword())
                        .build());
    }
}
