package com.github.altfatterz.springrefreshscopedemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RouteConfigRestController {

    private RouteConfig routeConfig;

    public RouteConfigRestController(RouteConfig routeConfig) {
        this.routeConfig = routeConfig;
    }

    @GetMapping("/route-config")
    public Map<String, Boolean> message() {
        return Map.of(
                "db-contract-route", routeConfig.getDbContractRoute(),
                "db-customers-routes", routeConfig.getDbCustomersRoute(),
                "db-accounts-route", routeConfig.getDbAccountsRoute()
        );
    }
}