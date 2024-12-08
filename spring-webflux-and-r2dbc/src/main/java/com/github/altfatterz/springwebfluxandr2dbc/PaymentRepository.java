package com.github.altfatterz.springwebfluxandr2dbc;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentRepository extends ReactiveCrudRepository<Payment, Long> {

    Flux<Payment> findAll();

    Mono<Payment> findById(Long id);
}
