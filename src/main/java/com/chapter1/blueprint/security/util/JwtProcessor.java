package com.chapter1.blueprint.security.util;

import com.chapter1.blueprint.security.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.Timestamp;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProcessor {

    private static final long ACCESS_TOKEN_VALID_MILLISECONDS = 1000L * 60 * 60 * 24; // 1 day
    private static final long REFRESH_TOKEN_VALID_MILLISECONDS = 1000L * 60 * 60 * 24 * 30; // 30 days

    @Value("${jwt.secretKey}")
    private String secretKey;
    private Key key;

    @Value("${encryption.secret}")
    private String encryptionSecret;

    private final CustomUserDetailsService userDetailsService;

    @PostConstruct
    private void init() {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException("Secret key must not be null or empty");
        }
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String memberId, Long uid, String auth, String memberName, String email) {
        String encryptedUid = AESUtil.encrypt(uid.toString(), encryptionSecret);

        return Jwts.builder()
                .setSubject(memberId)
                .claim("uid", encryptedUid)
                .claim("role", auth)
                .claim("memberName", memberName)
                .claim("email", email)
                .claim("tokenType", "ACCESS")
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + ACCESS_TOKEN_VALID_MILLISECONDS))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String memberId) {
        Timestamp expirationTimestamp = new Timestamp(new Date().getTime() + REFRESH_TOKEN_VALID_MILLISECONDS);

        return Jwts.builder()
                .setSubject(memberId)
                .claim("tokenType", "REFRESH")
                .setIssuedAt(new Date())
                .setExpiration(expirationTimestamp)
                .signWith(key)
                .compact();
    }

    public Timestamp getRefreshTokenExpiration() {
        return new Timestamp(new Date().getTime() + REFRESH_TOKEN_VALID_MILLISECONDS);
    }

    private Claims parseTokenClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUid(String token) {
        String encryptedUid = parseTokenClaims(token).get("uid", String.class);
        if (encryptedUid == null) {
            log.error("UID not found in token");
            throw new IllegalArgumentException("UID not found in token");
        }

        try {
            log.debug("Encrypted UID: {}", encryptedUid);
            String decryptedUid = AESUtil.decrypt(encryptedUid, encryptionSecret);
            log.debug("Decrypted UID: {}", decryptedUid);
            return Long.parseLong(decryptedUid);
        } catch (Exception e) {
            log.error("Failed to decrypt UID from token: " + e.getMessage(), e);
            throw new IllegalArgumentException("Invalid UID in token", e);
        }
    }

    public String getAuth(String token) {
        return parseTokenClaims(token).get("role", String.class);
    }

    public String getMemberName(String token) {
        return parseTokenClaims(token).get("memberName", String.class);
    }

    public String getEmail(String token) {
        return parseTokenClaims(token).get("email", String.class);
    }

    public String getSubject(String token) {
        return parseTokenClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseTokenClaims(token);
            return "ACCESS".equals(claims.get("tokenType")) && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Invalid Access Token: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = parseTokenClaims(refreshToken);
            return "REFRESH".equals(claims.get("tokenType")) && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Invalid Refresh Token: {}", e.getMessage());
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        String memberId = getSubject(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(memberId);
        return new UsernamePasswordAuthenticationToken(
                userDetails,  // principal
                token,        // credentials - 여기를 null에서 token으로 변경
                userDetails.getAuthorities()
        );
    }
}
