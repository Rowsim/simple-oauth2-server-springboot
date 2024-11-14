package com.example.auth_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.example.auth_service.models.AuthenticationCredentials;
import com.example.auth_service.models.GenerateAuthCodeDetails;
import com.example.auth_service.services.OAuth2Service;
import com.example.auth_service.services.UserValidationService;

@RestController
public class AuthController {

    private final UserValidationService userValidationService;
    private final OAuth2Service oAuth2Service;

    public AuthController(@Autowired UserValidationService userValidationService, @Autowired OAuth2Service oAuth2Service) {
        this.userValidationService = userValidationService;
        this.oAuth2Service = oAuth2Service;
    }

    @PostMapping("/public/authorize")
    public ModelAndView authorize(@RequestParam(value = "clientId") String clientId,
            @RequestParam(value = "redirectURL") String redirectURL,
            @RequestBody AuthenticationCredentials authCredentials) {
        // Would verify clientId and redirectURL possibly checking that the origin
        // is valid for given clientId and auth method is valid
        // oAuth2Service.validateClient

        if (userValidationService.validateUser(authCredentials.username(), authCredentials.password())) {
            final String authCode = oAuth2Service.generateAuthorizationCode(new GenerateAuthCodeDetails(clientId, redirectURL, authCredentials.username(), null));
            final String authorizationRedirectUrl = redirectURL + "?response_type=code&code=" + authCode;
            return new ModelAndView("redirect:" + authorizationRedirectUrl);
        } else {
            return new ModelAndView("error");
        }
    }
}
