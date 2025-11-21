package com.github.altfatterz.postgresqlr2dbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.Objects;

@Table(name = "customers")
public record Customer(@Id Long id, String firstname, String lastname, BigDecimal balance) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id) && Objects.equals(lastname, customer.lastname)
                && Objects.equals(firstname, customer.firstname) &&
                Objects.equals(balance, customer.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstname, lastname, balance);
    }
}
