package com.github.altfatterz.springwebfluxandjdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@RestController
public class PaymentRestController {

    private PaymentRepository paymentRepository;

    public PaymentRestController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/payments")
    public Flux<Payment> getPayments() {
        Flux<Payment> paymentsWrapper = Flux.defer(() -> Flux.fromIterable(this.paymentRepository.findAll()));
        return paymentsWrapper.subscribeOn(Schedulers.boundedElastic()).log();
    }

    @GetMapping("/payments/{id}")
    public Mono<Optional<Payment>> getPayments(@PathVariable Long id) {
        return Mono.fromCallable(() -> this.paymentRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic()).log();
    }

}
