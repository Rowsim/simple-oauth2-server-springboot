package com.example.auth_service.controllers;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth_service.models.Greeting;
import com.example.auth_service.services.OAuth2Service;

@RestController
public class GreetingController {

    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name,
            @RequestHeader(value = "Authorization") String authorizationHeader) {

        final String token = authorizationHeader.substring(7);
        final String userId = OAuth2Service.getUserIdFromToken(token);
        return new Greeting(counter.incrementAndGet(), String.format("Hello authenticated user: %s", userId));
    }
}
