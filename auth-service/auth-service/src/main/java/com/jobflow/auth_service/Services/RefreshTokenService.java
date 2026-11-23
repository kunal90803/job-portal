
package com.jobflow.auth_service.Services;

import com.jobflow.auth_service.Entity.RefreshToken;
import com.jobflow.auth_service.Entity.User;

import com.jobflow.auth_service.Repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Ref;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateRefreshToken(User user) {

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(
                Instant.now().plusMillis(refreshTokenExpiration)
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    public String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            return java.util.HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to hash refresh token", e
            );
        }
    }

    public Optional<RefreshToken> findByTokenHash(String hash){
        return refreshTokenRepository.findByTokenHash(hash);
    }

    public void saveToken(RefreshToken token){
        refreshTokenRepository.save(token);
    }


}
