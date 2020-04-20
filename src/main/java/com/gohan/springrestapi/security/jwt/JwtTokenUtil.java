package com.gohan.springrestapi.security.jwt;

import com.gohan.springrestapi.security.dto.Token;
import com.gohan.springrestapi.security.dto.TokenUserDetails;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    private final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${api-auth.jwt.token-secret}")
    private String tokenSecret;

    @Value("${api-auth.jwt.token-expiration-in-ms}")
    private Long tokenExpirationInMs;

    @Value("${api-auth.jwt.refresh-token-expiration-in-ms}")
    private Long refreshTokenExpirationInMs;

    public Token generateAccessToken(TokenUserDetails user) {
        Claims claims = Jwts.claims().setSubject(user.getUsername());

        claims.put("auth", user.getAuthorities());

        Date now = new Date();
        Long duration = now.getTime() + tokenExpirationInMs * 1000;
        Date expiryDate = new Date(duration);
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, tokenSecret)
                .compact();
        return new Token(Token.TokenType.ACCESS, token, duration, LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault()));
    }

    public Token generateRefreshToken(TokenUserDetails user) {
        Claims claims = Jwts.claims().setSubject(user.getUsername());

        claims.put("auth", user.getAuthorities());

        Date now = new Date();
        Long duration = now.getTime() + refreshTokenExpirationInMs * 1000;
        Date expiryDate = new Date(duration);
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, tokenSecret)
                .compact();
        return new Token(Token.TokenType.REFRESH, token, duration, LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault()));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(tokenSecret).parse(token);
            return true;
        } catch (SignatureException ex) {
            logger.warn("Invalid JWT Signature");
        } catch (MalformedJwtException ex) {
            logger.warn("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.warn("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.warn("Unsupported JWT exception");
        } catch (IllegalArgumentException ex) {
            logger.warn("Jwt claims string is empty");
        }
        return false;
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    public LocalDateTime getExpirationDateFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(token).getBody();
    }

}
