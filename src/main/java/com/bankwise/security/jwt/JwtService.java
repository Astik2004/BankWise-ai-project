package com.bankwise.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    private final SecretKey signingKey;

    private final Duration jwtExpiration;

    public JwtService(
            @Value("${application.security.jwt.secret-key}")
            String secretKey,

            @Value("${application.security.jwt.expiration}")
            long jwtExpiration
    ) {
        this.signingKey = createSigningKey(secretKey);
        this.jwtExpiration = Duration.ofMillis(jwtExpiration);    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    public Instant extractExpiration(String token) {
        return extractClaim(token, claims -> claims.getExpiration().toInstant());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {

        Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {

        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plus(jwtExpiration);
        String tokenId = UUID.randomUUID().toString();
        return Jwts.builder()
                .claims(extraClaims)
                .id(tokenId)
                .subject(userDetails.getUsername())
                .issuedAt(java.util.Date.from(issuedAt))
                .expiration(java.util.Date.from(expiration))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);

            return Objects.equals(
                    username,
                    userDetails.getUsername()
            ) && !isTokenExpired(token);

        } catch (JwtException | IllegalArgumentException exception) {
            log.debug("JWT validation failed: {}", exception.getMessage());
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException exception) {
            log.debug("JWT validation failed: {}", exception.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Instant expiration = extractExpiration(token);
        return !Instant.now().isBefore(expiration);
    }

    private Claims extractAllClaims(String token) {
        Jws<Claims> signedClaims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);

        return signedClaims.getPayload();
    }

    private SecretKey createSigningKey(String secretKey) {
        if (!StringUtils.hasText(secretKey)) {
            throw new IllegalStateException("JWT secret key must not be empty");
        }
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    private Duration validateExpiration(long expiration) {
        if (expiration <= 0) {
            throw new IllegalArgumentException("JWT expiration must be greater than zero");
        }
        return Duration.ofMillis(expiration);
    }
}