package com.example.auth_service.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;


@Service
public class UserValidationService {
    private final Map<String, String> userDatabase = new HashMap<>();

    public UserValidationService() {
        userDatabase.put("test-user-1", "password");
    }

    public boolean validateUser(String username, String password) {
        return password.equals(userDatabase.get(username));
    }
}
