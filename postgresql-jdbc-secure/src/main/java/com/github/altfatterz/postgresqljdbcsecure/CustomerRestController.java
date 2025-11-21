package com.github.altfatterz.postgresqljdbcsecure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CustomerRestController {

    private JdbcTemplate jdbcTemplate;

    public CustomerRestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/customers")
    public List<CustomerResponse> customers(@RequestParam String lastName) {
        return jdbcTemplate.query(
                        "SELECT id, firstname, lastname FROM customers WHERE lastname = ?", (rs, rowNum) ->
                                new Customer(
                                        rs.getLong("id"),
                                        rs.getString("firstname"),
                                        rs.getString("lastname")), lastName)
                .stream().map(it ->
                        new CustomerResponse(it.id(), it.firstname(), it.lastname())).toList();
    }

    record CustomerResponse(Long id, @JsonIgnore String firstName, @JsonIgnore String lastName) {
        public String getName() {
            return firstName + " " + lastName;
        }
    }
}

