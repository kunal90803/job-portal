package com.jobflow.auth_service.Controllers;

import com.jobflow.auth_service.DTOs.AuthParams;
import com.jobflow.auth_service.DTOs.LoginParams;
import com.jobflow.auth_service.Entity.User;
import com.jobflow.auth_service.Responses.LoginResponse;
import com.jobflow.auth_service.Services.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;

@RestController
@RequestMapping(value = "/api/v1/auth")
public class AuthController {
    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AuthParams authParams){
        try{
            authenticationService.registerUser(authParams);
            return ResponseEntity.ok("User Registered Successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration Failed : " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginParams loginParams){
        LoginResponse response = authenticationService.loginUser(loginParams);
        return ResponseEntity.ok(response);
    }

}
