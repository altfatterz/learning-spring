package org.example.springcloudstreamrabbitmqdemo;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.function.Consumer;

@Component
public class PaymentConsumer {

    @Bean
    public Consumer<Payment> payments() {
        return payment -> {
            System.out.println(payment.id());
            System.out.println(payment.amount());
        };
    }
}