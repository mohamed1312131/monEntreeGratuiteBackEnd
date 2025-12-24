package org.example.monentregratuit.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final Key key;
    private final long expirationMs;

    public JwtService(
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.expiration-ms}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(ensureBase64(secret)));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Date getExpiryFromNow() {
        return new Date(System.currentTimeMillis() + expirationMs);
    }

    private String ensureBase64(String maybePlain) {
        // If provided secret isn't base64, base64-encode compatible key material from it.
        // For simplicity, reuse as-is if it already looks base64 (contains '=' or is length%4==0).
        if (maybePlain == null) return "";
        String s = maybePlain.trim();
        if (s.isEmpty()) s = "development-secret-change-me";
        // naive check
        if (s.matches("[A-Za-z0-9+/=]+") && (s.length() % 4 == 0)) {
            return s;
        }
        return java.util.Base64.getEncoder().encodeToString(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
