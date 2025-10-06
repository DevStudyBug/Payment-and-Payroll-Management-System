package com.aurionpro.security;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.aurionpro.app.exception.JwtValidationException;

import io.jsonwebtoken.*;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String SECRET_KEY = "+GO8M3M0FaKU4zpUl9KrOXx+jMgCvB2C6NdHxOZR8JU=";

    // ✅ Generate JWT Token with roles included
    public String generateToken(UserDetails userDetails, Set<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        return generateToken(claims, userDetails);
    }

    // Generate JWT Token (with custom claims)
    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Validate the JWT Token
    public boolean isTokenValid(String jwtToken, UserDetails userDetails) {
        try {
            final String username = extractUserName(jwtToken);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(jwtToken));
        } catch (JwtValidationException e) {
            return false;
        }
    }

    // Extract any specific claim
    public <T> T extractClaim(String jwtToken, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(jwtToken);
        return claimsResolver.apply(claims);
    }

    public String extractUserName(String jwtToken) {
        return extractClaim(jwtToken, Claims::getSubject);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String jwtToken) {
        return (List<String>) extractAllClaims(jwtToken).get("roles");
    }

    private Claims extractAllClaims(String jwtToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtValidationException("JWT token has expired", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtValidationException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            throw new JwtValidationException("Invalid JWT token format", e);
        } catch (io.jsonwebtoken.security.SecurityException e) {
            throw new JwtValidationException("Invalid JWT signature", e);
        } catch (IllegalArgumentException e) {
            throw new JwtValidationException("JWT token is empty or null", e);
        }
    }

    private boolean isTokenExpired(String jwtToken) {
        return extractExpiration(jwtToken).before(new Date());
    }

    private Date extractExpiration(String jwtToken) {
        return extractClaim(jwtToken, Claims::getExpiration);
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
