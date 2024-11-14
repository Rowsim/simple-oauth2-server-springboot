package com.example.auth_service.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.auth_service.models.AuthCodeVerificationResult;
import com.example.auth_service.models.GenerateAuthCodeDetails;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class OAuth2Service {

    private final ConcurrentHashMap<String, GenerateAuthCodeDetails> authorizationCodes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> refreshTokens = new ConcurrentHashMap<>();

    public String generateAuthorizationCode(final GenerateAuthCodeDetails generateAuthCodeDetails) {
        final SecureRandom secureRandom = new SecureRandom();
        final byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);

        final String authorizationCode = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes) + "_" + generateAuthCodeDetails.clientId() + "_" + generateAuthCodeDetails.username();;
        final Long expiry = System.currentTimeMillis() + 10 * 60 * 1000;
        authorizationCodes.put(authorizationCode, new GenerateAuthCodeDetails(generateAuthCodeDetails.clientId(), generateAuthCodeDetails.redirectURL(), generateAuthCodeDetails.username(), Optional.of(expiry)));

        return authorizationCode;
    }

    public AuthCodeVerificationResult verifyAuthorizationCode(final String code, final String clientId, final String redirectURL) {
        final GenerateAuthCodeDetails authCodeDetails = authorizationCodes.get(code);
        if (authCodeDetails == null) {
            return new AuthCodeVerificationResult(false, null);
        }

        if (!authCodeDetails.clientId().equals(clientId) || !authCodeDetails.redirectURL().equals(redirectURL)) {
            return new AuthCodeVerificationResult(false, null);
        }

        if (authCodeDetails.expiration().isPresent() && authCodeDetails.expiration().get() < System.currentTimeMillis()) {
            authorizationCodes.remove(code);
            return new AuthCodeVerificationResult(false, null);
        }

        authorizationCodes.remove(code);

        return new AuthCodeVerificationResult(true, authCodeDetails);
    }

    public String generateRefreshToken(final String username) {
        final String refreshToken = UUID.randomUUID().toString();
        refreshTokens.put(refreshToken, username);
        return refreshToken;
    }

    public String findRefreshToken(final String refreshToken) {
        return refreshTokens.get(refreshToken);
    }

    public void removeRefreshToken(final String refreshToken) {
        refreshTokens.remove(refreshToken);
    }

    public static String generateAccessToken(final String userId) throws IOException, KeyLengthException, JOSEException {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userId)
                .expirationTime(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claimsSet);

        final String secretKey = new String(Files.readAllBytes(Paths.get("secret.key")));
        signedJWT.sign(new MACSigner(secretKey));

        return signedJWT.serialize();
    }

    public static boolean validateAccessToken(final String token) {
        try {
            final SignedJWT signedJWT = SignedJWT.parse(token);
            final String secretKey = new String(Files.readAllBytes(Paths.get("secret.key")));
            final MACVerifier verifier = new MACVerifier(secretKey);
            if (!signedJWT.verify(verifier)) {
                return false;
            }

            final JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            final Date expirationTime = claimsSet.getExpirationTime();
            if (expirationTime != null && expirationTime.before(new Date())) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getUserIdFromToken(final String token) {
        try {
            final SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
