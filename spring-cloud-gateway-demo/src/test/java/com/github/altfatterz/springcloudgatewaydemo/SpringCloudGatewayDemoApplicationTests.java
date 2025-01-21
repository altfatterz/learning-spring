package com.github.altfatterz.springcloudgatewaydemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@SpringBootTest(webEnvironment = DEFINED_PORT)
@ActiveProfiles("local")
class SpringCloudGatewayDemoApplicationTests {

    @Test
    public void apiKeyPresent() {
        WebTestClient.bindToServer().baseUrl("http://localhost:8081")
                .defaultHeader("x-api-key", "simple-key")
                .build()
                .get()
                .uri("/service/backend/api/v3/api-key")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void apiKeyMissing() {
        WebTestClient.bindToServer().baseUrl("http://localhost:8081")
                .build()
                .get()
                .uri("/service/backend/api/v3/api-key")
                .exchange()
                .expectStatus().isNotFound();
    }

}
