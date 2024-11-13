package com.github.altfatterz.postgresqlr2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

interface CustomerRepository extends R2dbcRepository<Customer, Long> {

    Flux<Customer> findByLastname(String lastname);

}
