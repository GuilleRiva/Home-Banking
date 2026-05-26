package com.home_banking_.service.impl;

import com.home_banking_.model.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;


@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    @Value("${jwt.secret}")
    private String secret;

    @Value("${application.security.jwt.expiration-ms:900000}")
    private long accessExpirationMs;

    @Value("${application.security.jwt.refresh-token.expiration-ms:1296000000}")
    private long refreshExpiration;


    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername (String token) {
        return extractClaim(token, Claims::getSubject); // getSubject = email
    }

    public Date extractExpiration (String token) {
        return extractClaim(token, Claims::getExpiration) ;
    }

    public <T> T extractClaim (String token, Function<Claims, T> resolver){
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }



    public String generateToken(UserDetails userDetails) {
        log.info("### generateToken(UserDetails) CALLED ### subject={}, expMs={}",
                userDetails.getUsername(),accessExpirationMs);
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList());

        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationMs){
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now  + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token){
       try {
           String username = extractUsername(token);
           return username != null && !isTokenExpired(token);
       } catch (JwtException | IllegalArgumentException e) {
           return false;
       }
    }


   private boolean isTokenExpired(String token) {
        Date exp = extractExpiration(token);
        if (exp == null) return true;
        return exp.before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public String generateRefreshToken (Users user){
        return createToken(Map.of(), user.getEmail(), refreshExpiration);
    }


    public List<String> getRoles(String token) {
        var claims = extractAllClaims(token);
        Object roles = claims.get("roles");

        if (roles instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return Collections.emptyList();
    }

}
