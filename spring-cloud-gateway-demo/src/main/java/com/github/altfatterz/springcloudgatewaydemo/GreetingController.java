package com.github.altfatterz.springcloudgatewaydemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GreetingController {

    @GetMapping("/api/v3/get")
    public String greet(@RequestParam(required = false) String name, @RequestHeader Map<String,String> headers) {
        System.out.println("I was called");
        System.out.println(headers);
        if (name != null) return "Hello " + name + "!";
        else return "Hello World!";
    }

    @GetMapping("/api/v3/api-key")
    public String foo(@RequestHeader Map<String,String> headers) {
        System.out.println("foo was called");
        System.out.println(headers);
        return headers.toString();
    }
}
