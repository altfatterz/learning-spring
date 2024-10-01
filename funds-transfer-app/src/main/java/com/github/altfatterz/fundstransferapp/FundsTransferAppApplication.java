package com.github.altfatterz.fundstransferapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@SpringBootApplication
@RestController
public class FundsTransferAppApplication {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        SpringApplication.run(FundsTransferAppApplication.class, args);
    }

    @GetMapping("/balance")
    public Double getBalance() {
        return jdbcTemplate.queryForObject("select balance FROM balances WHERE customer_id = ?", Double.class, getCustomerId());
    }

    @PostMapping("/account-transfer")
    public void transfer(@RequestBody TransferEvent transferEvent) {
        jdbcTemplate.update("update balances set balance = balance - ? where customer_id = ?", transferEvent.amount(), getCustomerId());
        jdbcTemplate.update("update balances set balance = balance + ? where customer_id = ?", transferEvent.amount(), transferEvent.to());
    }

    private String getCustomerId() {
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
    }
}


record TransferEvent(String to, Double amount) {
}

