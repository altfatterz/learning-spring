package org.example.springbootjwtdemo;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/private")
    public String privateMessage(@AuthenticationPrincipal Jwt jwt) {
        // You can access claims directly from the injected Jwt object
        String userId = jwt.getSubject();
        return "Hello, " + userId + "! You have a valid JWT.";
    }

    @GetMapping("/public")
    public String publicMessage() {
        return "This is a public endpoint. No JWT needed.";
    }

}
