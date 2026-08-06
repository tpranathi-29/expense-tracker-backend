package com.example.expense_tracker.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.util.Date;

@Component
public class JwtUtil {

    private static final SecretKey SECRET_KEY =
            Keys.hmacShaKeyFor(
                    "mysecretkeymysecretkeymysecretkey"
                            .getBytes()
            );

    // Generate JWT Token
    public String generateToken(String email) {

        return Jwts.builder()

                .subject(email)

                .issuedAt(new Date())

                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 1000 * 60 * 60
                        )
                )

                .signWith(
                        SECRET_KEY,
                        SignatureAlgorithm.HS256
                )

                .compact();
    }

    // Extract Email
    public String extractEmail(String token) {

        Claims claims = Jwts.parser()

                .verifyWith(SECRET_KEY)

                .build()

                .parseSignedClaims(token)

                .getPayload();

        return claims.getSubject();
    }

    // Extract All Claims
    public Claims extractClaims(String token) {

        return Jwts.parser()

                .verifyWith(SECRET_KEY)

                .build()

                .parseSignedClaims(token)

                .getPayload();
    }

    // Check Token Expiry
    public boolean isTokenExpired(String token) {

        return extractClaims(token)

                .getExpiration()

                .before(new Date());
    }

    // Validate Token
    public boolean validateToken(String token) {

        try {

            return !isTokenExpired(token);

        } catch (Exception e) {

            return false;

        }

    }

}