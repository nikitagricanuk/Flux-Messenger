package ru.flux.flux.messenger.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.exceptions.RegistrationTokenExpiredException;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    public static final String PURPOSE_CLAIM = "purpose";
    public static final String PURPOSE_OAUTH_REGISTER = "oauth-register";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${jwt.registration-expiration-ms:600000}")
    private long registrationExpirationMs;

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User customUserDetails) {
            claims.put("id", customUserDetails.getId());
            claims.put("phone", customUserDetails.getPhone());
        }
        return buildToken(claims, userDetails.getUsername(), expirationMs);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails.getUsername(), refreshExpirationMs);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractSubject(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String generateRegistrationToken(String subject, Map<String, Object> claims) {
        Map<String, Object> allClaims = new HashMap<>(claims);
        allClaims.put(PURPOSE_CLAIM, PURPOSE_OAUTH_REGISTER);
        return buildToken(allClaims, subject, registrationExpirationMs);
    }

    public Claims parseRegistrationToken(String token) {
        Claims claims;
        try {
            claims = extractAllClaims(token);
        } catch (ExpiredJwtException e) {
            throw new RegistrationTokenExpiredException("Registration token has expired");
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid registration token");
        }
        Object purpose = claims.get(PURPOSE_CLAIM);
        if (!PURPOSE_OAUTH_REGISTER.equals(purpose)) {
            throw new IllegalArgumentException("Token is not a registration token");
        }
        return claims;
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private java.security.Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}