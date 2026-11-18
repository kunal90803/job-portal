package com.jobflow.auth_service.Services;

import com.jobflow.auth_service.DTOs.AuthParams;
import com.jobflow.auth_service.DTOs.LoginParams;
import com.jobflow.auth_service.DTOs.UserInfo;
import com.jobflow.auth_service.Entity.User;
import com.jobflow.auth_service.Repository.UserRepository;
import com.jobflow.auth_service.Responses.LoginResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User registerUser(AuthParams authParams) {

        // 1. Check if email already exists
        if (userRepository.existsByEmail(authParams.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        // 2. Create new user
        User user = new User();
        user.setName(authParams.getName());
        user.setEmail(authParams.getEmail());
        user.setPassword(passwordEncoder.encode(authParams.getPassword()));
        user.getRoles().add(authParams.getRoles());
        return userRepository.save(user);
    }

    public LoginResponse loginUser(LoginParams loginParams) {
        User user = userRepository.findByEmail(loginParams.getEmail());
        if(user==null){
            throw new RuntimeException("User doesn't exist for this email");
        }
        boolean matches = passwordEncoder.matches(loginParams.getPassword(), user.getPassword());
        if(!matches){
            throw new RuntimeException("Password Incorrect");
        }

        // Generate JWT
        String token = jwtService.generateToken(user.getId(),user.getEmail());

        // Prepare user information
        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles()
        );

        // Return login response
        return new LoginResponse(
                token,
                "Bearer",
                userInfo
        );
    }


}
