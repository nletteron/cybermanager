package com.cybermanager.infrastructure.out.adapters.security.users;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;

@Component
public class JwtAccessTokenReader {

    private final SecretKey secretKey;
    private final Instant startedAt;

    public JwtAccessTokenReader(@Value("${cybermanager.security.jwt-secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.startedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }

    public AuthenticatedActor read(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (claims.getIssuedAt() == null || claims.getIssuedAt().toInstant().isBefore(startedAt)) {
            throw new JwtException("Token issued before business server startup");
        }
        List<?> roles = claims.get("roles", List.class);
        return new AuthenticatedActor(
                claims.get("email", String.class),
                roles == null ? java.util.Set.of() : roles.stream().map(String::valueOf).collect(java.util.stream.Collectors.toCollection(HashSet::new))
        );
    }

}

