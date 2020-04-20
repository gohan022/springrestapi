package com.gohan.springrestapi.security.jwt;

import com.gohan.springrestapi.security.CustomUserDetailsService;
import com.gohan.springrestapi.security.SessionService;
import com.gohan.springrestapi.security.dto.Token;
import com.gohan.springrestapi.security.dto.TokenUserDetails;
import com.gohan.springrestapi.security.util.CookieUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    @Value("${api-auth.cookie.access-token-cookie-name}")
    private String accessTokenCookieName;

    @Value("${api-auth.cookie.refresh-token-cookie-name}")
    private String refreshTokenCookieName;

    private static final String[] NEW_TOKEN_IGNORE = {"/auth/**", "/register"};

    private final JwtTokenUtil jwtTokenUtil;

    private final CookieUtil cookieUtil;

    private final CustomUserDetailsService userDetailsService;

    private final AccountStatusUserDetailsChecker accountStatusChecker;

    private final SessionService sessionService;

    public JwtTokenAuthenticationFilter(JwtTokenUtil jwtTokenUtil, CookieUtil cookieUtil,
                                        CustomUserDetailsService userDetailsService,
                                        AccountStatusUserDetailsChecker accountStatusChecker,
                                        SessionService sessionService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.cookieUtil = cookieUtil;
        this.userDetailsService = userDetailsService;
        this.accountStatusChecker = accountStatusChecker;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromCookie(request);
            UsernamePasswordAuthenticationToken authentication = null;

            if (StringUtils.hasText(jwt)) {
                if (jwtTokenUtil.validateToken(jwt)) {
                    //System.out.println("Authentication Filter");
                    String username = jwtTokenUtil.getUsernameFromToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    accountStatusChecker.check(userDetails);
                    authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Renew Access Token & update DB session if previous access token is present otherwise throw error
                    if(Arrays.stream(NEW_TOKEN_IGNORE).noneMatch(e -> new AntPathMatcher().match(e, request.getRequestURI()))) {
                        Token newAccessToken = jwtTokenUtil.generateAccessToken((TokenUserDetails) userDetails);
                        if(this.sessionService.isValid(request, jwt, newAccessToken.getTokenValue(), ((TokenUserDetails) userDetails).getUser())) {
                            response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(newAccessToken.getTokenValue(), newAccessToken.getDuration()).toString());
                        } else {
                            response.addHeader("X-AUTHENTICATION", "INVALID_SESSION");
                            throw new Exception("INVALID_SESSION");
                        }
                    }
                }
            }

            if(authentication == null) {
                SecurityContextHolder.clearContext();
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (accessTokenCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
