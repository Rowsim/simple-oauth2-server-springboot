package com.example.auth_service.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth_service.models.AuthCodeVerificationResult;
import com.example.auth_service.models.CodeExchangeRequestBody;
import com.example.auth_service.services.OAuth2Service;
import com.nimbusds.jose.JOSEException;

@RestController
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    public OAuth2Controller(@Autowired OAuth2Service oAuth2Service) {
        this.oAuth2Service = oAuth2Service;
    }

    @PostMapping("/public/token")
    public ResponseEntity<Map<String, String>> exchangeCodeForToken(@RequestBody CodeExchangeRequestBody codeExchangeRequestBody) {
        final AuthCodeVerificationResult authCodeVerificationResult = oAuth2Service.verifyAuthorizationCode(
                codeExchangeRequestBody.code(),
                codeExchangeRequestBody.clientId(),
                codeExchangeRequestBody.redirectURL()
        );
        if (!authCodeVerificationResult.isValid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid_grant"));
        }

        try {
            final String accessToken = OAuth2Service.generateAccessToken(authCodeVerificationResult.generateAuthCodeDetails().username());
            final String refreshToken = oAuth2Service.generateRefreshToken(authCodeVerificationResult.generateAuthCodeDetails().username());
            Map<String, String> response = new HashMap<>();
            response.put("access_token", accessToken);
            response.put("refresh_token", refreshToken);
            response.put("token_type", "Bearer");
            response.put("expires_in", "300");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
        } catch (JOSEException | IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "access_token_error"));
        }
    }

    @PostMapping("/public/refresh")
    public ResponseEntity<Map<String, String>> exchangeRefreshTokenForAccessToken(@RequestBody HashMap<String, String> refreshTokenExchangeBody) {
        try {
            final String refreshToken = refreshTokenExchangeBody.get("refreshToken");
            final String tokenUsername = oAuth2Service.findRefreshToken(refreshToken);
            if (tokenUsername == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid_grant"));
            }
            oAuth2Service.removeRefreshToken(refreshToken);
            final String accessToken = OAuth2Service.generateAccessToken(tokenUsername);
            final String newRefreshToken = oAuth2Service.generateRefreshToken(tokenUsername);
            Map<String, String> response = new HashMap<>();
            response.put("access_token", accessToken);
            response.put("refresh_token", newRefreshToken);
            response.put("token_type", "Bearer");
            response.put("expires_in", "300");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
        } catch (JOSEException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "access_token_error"));
        }
    }
}
