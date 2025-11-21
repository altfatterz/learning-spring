package com.github.altfatterz.vaultpostgresqlr2dbcdemo.config;

import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcConnectionDetails;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.function.Predicate;
import java.util.function.Supplier;

@Configuration
public class DatabaseConfig {

    @Bean
    public VaultR2dbcConnectionDetails vaultR2dbcConnectionDetails(
            DBConnectionProperties dbConnectionProperties,
            R2dbcProperties r2dcProperties) {
        return new VaultR2dbcConnectionDetails(dbConnectionProperties, r2dcProperties);
    }

    static class VaultR2dbcConnectionDetails implements R2dbcConnectionDetails {

        private DBConnectionProperties dbConnectionProperties;
        private R2dbcProperties r2dbcProperties;

        public VaultR2dbcConnectionDetails(DBConnectionProperties dbConnectionProperties,
                                           R2dbcProperties r2dbcProperties) {
            this.dbConnectionProperties = dbConnectionProperties;
            this.r2dbcProperties = r2dbcProperties;
        }

        @Override
        public ConnectionFactoryOptions getConnectionFactoryOptions() {
            ConnectionFactoryOptions urlOptions = ConnectionFactoryOptions.parse(dbConnectionProperties.getUrl());
            ConnectionFactoryOptions.Builder optionsBuilder = urlOptions.mutate();
            configureIf(optionsBuilder, urlOptions, ConnectionFactoryOptions.USER, dbConnectionProperties::getUsername,
                    StringUtils::hasText);
            configureIf(optionsBuilder, urlOptions, ConnectionFactoryOptions.PASSWORD, dbConnectionProperties::getPassword,
                    StringUtils::hasText);
            configureIf(optionsBuilder, urlOptions, ConnectionFactoryOptions.DATABASE,
                    () -> determineDatabaseName(this.r2dbcProperties), StringUtils::hasText);
            if (this.r2dbcProperties.getProperties() != null) {
                this.r2dbcProperties.getProperties()
                        .forEach((key, value) -> optionsBuilder.option(Option.valueOf(key), value));
            }
            return optionsBuilder.build();
        }

        private <T extends CharSequence> void configureIf(ConnectionFactoryOptions.Builder optionsBuilder,
                                                          ConnectionFactoryOptions originalOptions, Option<T> option, Supplier<T> valueSupplier,
                                                          Predicate<T> setIf) {
            if (originalOptions.hasOption(option)) {
                return;
            }
            T value = valueSupplier.get();
            if (setIf.test(value)) {
                optionsBuilder.option(option, value);
            }
        }

        private String determineDatabaseName(R2dbcProperties properties) {
            if (properties.isGenerateUniqueName()) {
                return properties.determineUniqueName();
            }
            if (StringUtils.hasLength(properties.getName())) {
                return properties.getName();
            }
            return null;
        }
    }

}
