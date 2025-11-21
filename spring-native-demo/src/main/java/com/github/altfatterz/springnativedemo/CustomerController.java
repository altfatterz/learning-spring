package com.github.altfatterz.springnativedemo;

import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CustomerController {

    private static final List<Customer> customers = List.of(new Customer("John", "Doe"), new Customer("Jane", "Doe"));

    @GetMapping("/customers")
    public List<Customer> getCustomers() {
        return customers;
    }

    // works because the Customer is already used in method signature of a Spring Controller
    @GetMapping("/customer-fields")
    public List<String> getCustomerFields() {
        List<String> fieldNames = new ArrayList<>();
        ReflectionUtils.doWithFields(Customer.class, field -> fieldNames.add(field.getName()));
        return fieldNames;
    }

    // will return empty array, is not seen by the AOT module, not in the `reflect-config.json` file
    // easy way to solve is to add `@RegisterReflectionForBinding(InternalCustomer.class)`
    // or use programmatically with `@ImportRuntimeHints`
    @GetMapping("/internal-customer-fields")
    public List<String> internalCustomerFields() {
        List<String> fieldNames = new ArrayList<>();
        ReflectionUtils.doWithFields(InternalCustomer.class, field -> fieldNames.add(field.getName()));
        return fieldNames;
    }

}
