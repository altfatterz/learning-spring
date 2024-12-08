package com.github.altfatterz.springwebfluxandjdbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "payments")
public record Payment(@Id Long id, String payer, String payee, Double amount) {

}
