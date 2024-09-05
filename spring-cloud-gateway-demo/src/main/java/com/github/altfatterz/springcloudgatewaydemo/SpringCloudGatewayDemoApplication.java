package com.github.altfatterz.springcloudgatewaydemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;

@SpringBootApplication
public class SpringCloudGatewayDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudGatewayDemoApplication.class, args);
    }

}
