package com.example.auth_service.models;

import java.util.Optional;

public record GenerateAuthCodeDetails(String clientId, String redirectURL, String username, Optional<Long> expiration) {

}
