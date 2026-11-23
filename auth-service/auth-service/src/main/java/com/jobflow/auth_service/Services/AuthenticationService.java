package com.jobflow.auth_service.Services;

import com.jobflow.auth_service.DTOs.AuthParams;
import com.jobflow.auth_service.DTOs.LoginParams;
import com.jobflow.auth_service.DTOs.UserInfo;
import com.jobflow.auth_service.Entity.RefreshToken;
import com.jobflow.auth_service.Entity.User;
import com.jobflow.auth_service.Repository.UserRepository;
import com.jobflow.auth_service.Responses.LoginResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;


@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenService refreshTokenService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public User findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }
    public User findUserById(String id){
        Optional<User> user = userRepository.findById(id);

        return user.orElseThrow(
                () -> new RuntimeException("User not found with id: " + id)
        );
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
        // Generate access token (JWT)
        String accessToken = jwtService.generateToken(
                user.getId(),
                user.getEmail()
        );

        // Generate refresh token and save its hash in MySQL
        String refreshToken =
                refreshTokenService.generateRefreshToken(user);

        // Prepare user information
        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles()
        );

        // Return both tokens
        return new LoginResponse(
                accessToken,
                "Bearer",
                userInfo,
                jwtService.getExpirationSeconds(),
                refreshToken
        );
    }


    @Transactional
    public LoginResponse refreshTokens(String rawRefreshToken) {

        // 1. Hash the incoming refresh token
        String tokenHash =refreshTokenService.hashToken(rawRefreshToken);

        // 2. Find the token in database
        RefreshToken oldToken = refreshTokenService
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token"
                ));

        // 3. Validate token status
        if (oldToken.isRevoked()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token has already been revoked"
            );
        }

        if (!Instant.now().isBefore(oldToken.getExpiresAt())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token has expired"
            );
        }

        // 4. Get associated user
        User user = oldToken.getUser();

        // 5. Revoke the old refresh token
        oldToken.setRevokedAt(Instant.now());
        refreshTokenService.saveToken(oldToken);

        // 6. Generate new access token
        String newAccessToken = jwtService.generateToken(
                user.getId(),
                user.getEmail()
        );

        // 7. Generate new refresh token
        // Its hash will automatically be saved in MySQL
        String newRefreshToken =
                refreshTokenService.generateRefreshToken(user);

        // 8. Prepare user information
        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles()
        );

        // 9. Return new token pair
        return new LoginResponse(
                newAccessToken,
                "Bearer",
                userInfo,
                jwtService.getExpirationSeconds(),
                newRefreshToken
        );
    }


    public void revokeToken(String rawToken) {

        String tokenHash = refreshTokenService.hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenService
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token"
                ));

        if (refreshToken.isRevoked()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token already revoked"
            );
        }

        if (!Instant.now().isBefore(refreshToken.getExpiresAt())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token expired"
            );
        }
        refreshToken.setRevokedAt(Instant.now());
        refreshTokenService.saveToken(refreshToken);
    }






}
