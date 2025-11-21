package com.github.altfatterz.vaultpostgresqlr2dbcdemo.customer;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

interface CustomerRepository extends ReactiveCrudRepository<Customer, Long> {

    Flux<Customer> findByLastname(String lastname);

}
