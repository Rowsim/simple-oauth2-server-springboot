package com.example.auth_service.controllers;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth_service.models.Greeting;

@RestController
public class PublicGreetingController {

    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/public/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format("Hello, %s!", name));
    }
}
