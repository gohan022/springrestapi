package com.gohan.springrestapi.security.jwt;

import com.gohan.springrestapi.security.TokenUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    private final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    private Clock clock = DefaultClock.INSTANCE;

    @Value("${api-auth.jwt.token-secret}")
    private String tokenSecret;

    @Value("${api-auth.jwt.header-string}")
    private String tokenHeader;

    @Value("${api-auth.jwt.token-prefix}")
    private String tokenPrefix;

    @Value("${api-auth.jwt.token-expiration-in-ms}")
    private Long tokenExpirationInMs;

    @Value("${api-auth.jwt.refresh-token-expiration-in-ms}")
    private Long refreshTokenExpirationInMs;

    public String generateAccessToken(TokenUserDetails user) {
        Claims claims = Jwts.claims().setSubject(user.getUsername());

        claims.put("auth", user.getAuthorities());

        final Date now = clock.now();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenExpirationInMs * 1000))
                .signWith(SignatureAlgorithm.HS512, tokenSecret)
                .compact();
    }

    public String generateRefreshToken(TokenUserDetails user) {
        Claims claims = Jwts.claims().setSubject(user.getUsername());

        claims.put("auth", user.getAuthorities());

        final Date now = clock.now();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenExpirationInMs * 1000))
                .signWith(SignatureAlgorithm.HS512, tokenSecret)
                .compact();
    }

    public boolean validateToken(String token, TokenUserDetails user) {
        final String username = getUsernameFromToken(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(clock.now());
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(token).getBody();
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

        return null;
    }

}
