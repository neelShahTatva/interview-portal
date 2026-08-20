package com.tatvasoft.interview_portal.util;

import com.tatvasoft.interview_portal.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())

                .claim("UserId", user.getId())
                .claim("RoleId", user.getRole().getId())
                .claim("Name", user.getUsername())
                .claim("LoginTimeStamp", String.valueOf(System.currentTimeMillis()))
                .claim("IsAdmin", user.getRole().getRoleName().equalsIgnoreCase("admin"))
                .claim("IsInterviewer", user.getRole().getRoleName().equalsIgnoreCase("interviewer"))
                .claim("RoleName", user.getRole().getRoleName())
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 4)) // 4 hours
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .claim("UserId", user.getId())
                .claim("RoleId", user.getRole().getId())
                .claim("Name", user.getUsername())
                .claim("LoginTimeStamp", String.valueOf(System.currentTimeMillis()))
                .claim("IsAdmin", user.getRole().getRoleName().equalsIgnoreCase("admin"))
                .claim("IsInterviewer", user.getRole().getRoleName().equalsIgnoreCase("interviewer"))
                .claim("RoleName", user.getRole().getRoleName())
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7 day
                .signWith(getKey())
                .compact();
    }

    public String extractType(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("type", String.class);
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Long extractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("UserId", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}