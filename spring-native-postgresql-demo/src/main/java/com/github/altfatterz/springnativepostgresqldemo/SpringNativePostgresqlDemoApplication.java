package com.github.altfatterz.springnativepostgresqldemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SpringNativePostgresqlDemoApplication {

    @GetMapping("/")
    String home() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringNativePostgresqlDemoApplication.class, args);
    }

}

@RestController
class CustomerHttpController {

    final private CustomerRepository customerRepository;

    public CustomerHttpController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/customers")
    Iterable<Customer> customers() {
        return customerRepository.findAll();
    }
}

interface CustomerRepository extends CrudRepository<Customer, Integer> {
}

record Customer(@Id Integer id, String name) {
}
