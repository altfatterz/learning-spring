package com.github.altfatterz.postgresqlr2dbc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

@RestController
public class CustomerRestController {

    private CustomerRepository customerRepository;

    public CustomerRestController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/customers")
    public Flux<CustomerResponse> customers(@RequestParam String lastName) {
        return customerRepository.findByLastname(lastName).map(it ->
                new CustomerResponse(it.id(), it.firstname(), it.lastname(), it.balance()));
    }

    record CustomerResponse(Long id, @JsonIgnore String firstName,
                            @JsonIgnore String lastName, BigDecimal balance) {
        public String getName() {
            return firstName + " " + lastName + " :" + balance;
        }
    }
}

