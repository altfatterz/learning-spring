package com.github.altfatterz.springnativedemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

// not yet working with
// mvn -PnativeTest test

@WebMvcTest()
class DynamicControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void canSeeInternalCustomerFields() throws Exception {
        mockMvc.perform(get("/internal-customer-fields")).andExpect(content().json("""
                ["firstName","lastName"] 
                """));
    }
}
