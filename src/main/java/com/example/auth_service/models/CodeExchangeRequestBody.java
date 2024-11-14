package com.example.auth_service.models;

public record CodeExchangeRequestBody(String code, String clientId, String redirectURL) {

}
