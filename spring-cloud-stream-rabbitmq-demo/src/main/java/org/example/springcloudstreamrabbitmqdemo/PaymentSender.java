package org.example.springcloudstreamrabbitmqdemo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class PaymentSender {

    private final StreamBridge streamBridge;
    private final String destination = "payments-out";
    private final String secureDestination = "secure-payments-out";

    public PaymentSender(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Bean
    CommandLineRunner sendToDestinationOnStartup() {
        return args -> {
            Payment payment = new Payment("p-123", 99.99);
            streamBridge.send(destination, MessageBuilder.withPayload(payment).build());
            System.out.println("Sent payment: " + payment.id() + " amount=" + payment.amount());
        };
    }

    @Bean
    CommandLineRunner sendToSecureDestinationOnStartup() {
        return args -> {
            Payment payment = new Payment("p-secure", 99.99);
            streamBridge.send(secureDestination, MessageBuilder.withPayload(payment).build());
            System.out.println("Sent secure payment: " + payment.id() + " amount=" + payment.amount());
        };
    }
}
