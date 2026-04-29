package com.apiPersistence.intelligenceQuery.service;

import com.apiPersistence.intelligenceQuery.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenTtlSeconds;
    private final String issuer;

    public JwtService(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.access-token-ttl-seconds:900}") long accessTokenTtlSeconds,
            @Value("${app.jwt.issuer:intelligenceQuery-api}") String issuer
    ) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.issuer = issuer;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expire = now.plusSeconds(accessTokenTtlSeconds);

        return Jwts.builder()
                .subject(resolveSubject(user))
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expire))
                .claim("username", user.getUsername())
                .claim("role", user.getRole() != null ? user.getRole().name() : null)
                .signWith(secretKey)
                .compact();
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractUsername(String token) {
        Object username = parseClaims(token).get("username");
        return username != null ? username.toString() : null;
    }

    public String extractRole(String token) {
        Object role = parseClaims(token).get("role");
        return role != null ? role.toString() : null;
    }

    public Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration() != null
                    && claims.getExpiration().after(new Date())
                    && issuer.equals(claims.getIssuer());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String resolveSubject(User user) {
        // Prefer internal UUID if present; fallback to githubId
        UUID id = user.getId();
        if (id != null) return id.toString();
        return user.getGithubId();
    }

    public UUID extractUserId(String token) {
        String subject = extractSubject(token);
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
