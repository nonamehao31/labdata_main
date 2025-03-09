package com.example.labdata.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationInMs;

    // Generate a token from Authentication object
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    // Get user ID from JWT token
    public Long getUserIdFromJWT(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    // Validate JWT token
    public boolean validateToken(String authToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            
            // 获取当前时间和令牌过期时间，用于调试
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(authToken)
                    .getBody();
            Date expiration = claims.getExpiration();
            Date issuedAt = claims.getIssuedAt();
            Date now = new Date();
            
            // 记录详细的时间信息用于调试
            logger.info("令牌验证信息 - 当前服务器时间: {} ({})", now, now.getTime());
            logger.info("令牌验证信息 - 令牌颁发时间: {} ({})", issuedAt, issuedAt.getTime());
            logger.info("令牌验证信息 - 令牌过期时间: {} ({})", expiration, expiration.getTime());
            logger.info("令牌验证信息 - 距离过期还有: {} 毫秒", expiration.getTime() - now.getTime());
            
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            // 添加过期令牌的详细信息
            Date expiration = ex.getClaims().getExpiration();
            Date now = new Date();
            logger.error("Expired JWT token - 过期时间: {} ({}), 当前时间: {} ({}), 过期了: {} 毫秒", 
                    expiration, expiration.getTime(), now, now.getTime(), now.getTime() - expiration.getTime());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty");
        }
        return false;
    }
}
