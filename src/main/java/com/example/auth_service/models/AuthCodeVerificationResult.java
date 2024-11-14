package com.example.auth_service.models;

public record AuthCodeVerificationResult(boolean isValid, GenerateAuthCodeDetails generateAuthCodeDetails) {

}
