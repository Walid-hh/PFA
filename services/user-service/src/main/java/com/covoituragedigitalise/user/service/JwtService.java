package com.covoituragedigitalise.user.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        try {
            String username = extractClaim(token, Claims::getSubject);
            System.out.println("🔍 JwtService - Username extrait: " + username);
            return username;
        } catch (Exception e) {
            System.err.println("❌ JwtService - Erreur extraction username: " + e.getMessage());
            throw e;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    // ✅ MÉTHODE pour générer token avec email String
    public String generateToken(String email) {
        try {
            Map<String, Object> extraClaims = new HashMap<>();
            String token = Jwts
                    .builder()
                    .claims(extraClaims)
                    .subject(email)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(getSignInKey())
                    .compact();

            System.out.println("✅ JwtService - Token généré pour: " + email);
            return token;
        } catch (Exception e) {
            System.err.println("❌ JwtService - Erreur génération token: " + e.getMessage());
            throw e;
        }
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    // ✅ MÉTHODE ESSENTIELLE pour valider le token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
            System.out.println("🔍 JwtService - Token valid pour " + username + ": " + isValid);
            return isValid;
        } catch (Exception e) {
            System.err.println("❌ JwtService - Token invalide: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            boolean expired = extractExpiration(token).before(new Date());
            System.out.println("🕐 JwtService - Token expiré: " + expired);
            return expired;
        } catch (Exception e) {
            System.err.println("❌ JwtService - Erreur vérification expiration: " + e.getMessage());
            return true;
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        try {
            Claims claims = Jwts
                    .parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            System.out.println("✅ JwtService - Claims extraits avec succès");
            return claims;

        } catch (ExpiredJwtException e) {
            System.err.println("❌ JwtService - Token expiré");
            throw new RuntimeException("Token expiré", e);
        } catch (UnsupportedJwtException e) {
            System.err.println("❌ JwtService - Token non supporté");
            throw new RuntimeException("Token non supporté", e);
        } catch (MalformedJwtException e) {
            System.err.println("❌ JwtService - Token malformé");
            throw new RuntimeException("Token malformé", e);
        } catch (SecurityException e) {
            System.err.println("❌ JwtService - Signature invalide");
            throw new RuntimeException("Signature du token invalide", e);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ JwtService - Token vide");
            throw new RuntimeException("Token vide", e);
        }
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}