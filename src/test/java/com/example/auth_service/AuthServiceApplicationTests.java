package com.example.auth_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class AuthServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void noAuthTokenHeaderShouldReturn401OnNonPublicPath() throws Exception {
        this.mockMvc.perform(get("/greeting")).andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    public void noParamGreetingShouldReturnDefaultMessage() throws Exception {
        this.mockMvc.perform(get("/public/greeting")).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, World!"));
    }

    @Test
    public void paramGreetingShouldReturnTailoredMessage() throws Exception {
        final String paramValue = "test";
        final String expectedContent = String.format("Hello, %s!", paramValue);

        this.mockMvc.perform(get("/public/greeting").param("name", paramValue)).andDo(print())
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").value(expectedContent));
    }
}
