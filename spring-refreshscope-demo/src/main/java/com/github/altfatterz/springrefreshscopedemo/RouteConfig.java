package com.github.altfatterz.springrefreshscopedemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@RefreshScope
@Component
public class RouteConfig {

    @Value("${route-config.db-contract-route}")
    private Boolean dbContractRoute;

    @Value("${route-config.db-customers-route}")
    private Boolean dbCustomersRoute;

    @Value("${route-config.db-accounts-route}")
    private Boolean dbAccountsRoute;

    public Boolean getDbContractRoute() {
        return dbContractRoute;
    }

    public Boolean getDbCustomersRoute() {
        return dbCustomersRoute;
    }

    public Boolean getDbAccountsRoute() {
        return dbAccountsRoute;
    }


}
