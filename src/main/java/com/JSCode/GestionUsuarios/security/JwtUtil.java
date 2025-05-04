package com.JSCode.GestionUsuarios.security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    @Autowired
    public JwtUtil(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    private static final long EXPIRATION_MS = 3600000;

    public String generateToken(Long userId, List<Long> roleIds) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", roleIds)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRecoveryToken(Long id){
        return Jwts.builder()
                .subject(id.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + (EXPIRATION_MS/2)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateVerificationToken(String email){
        return Jwts.builder()
        .subject(email)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
        .signWith(secretKey, Jwts.SIG.HS256)
        .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            
            return !claims.getPayload().getExpiration().before(new Date());
        } catch (JwtException e) {
            return false; 
        }
    }
}       