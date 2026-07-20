package com.ritnesh.careerpilot.util;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET_KEY =
            "careerpilotbackendprojectjwtsecretkey2026secure";

    private static final long EXPIRATION_TIME = 1000 * 60 * 60;
    public String generateToken(String email) {

        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }
}
