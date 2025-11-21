package com.github.altfatterz.springwebfluxandr2dbc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class PaymentRestController {

    private PaymentRepository paymentRepository;

    public PaymentRestController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/payments")
    public Flux<Payment> getPayments() {
        return paymentRepository.findAll().log();
    }

    @GetMapping("/payments/{id}")
    public Mono<Payment> getPayments(@PathVariable Long id) {
        return paymentRepository.findById(id).log();
    }

}
