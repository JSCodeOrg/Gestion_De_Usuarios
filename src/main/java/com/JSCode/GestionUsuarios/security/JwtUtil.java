package com.JSCode.GestionUsuarios.security;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.util.Date;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {
         private static final SecretKey key = Jwts.SIG.HS256.key().build();

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return parseToken(token).getPayload().getSubject();
    }

    public boolean validateToken(String token, String username) {
        return (username.equals(extractUsername(token)) && !isTokenExpired(token));
    }

    private Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = parseToken(token).getPayload().getExpiration();
        return expiration.before(new Date());
    }
}       