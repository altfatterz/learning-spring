package com.github.altfatterz.vault;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table(name = "customers")
public record Customer(@Id Long id, String firstname, String lastname) {
}
