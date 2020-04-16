package com.gohan.springrestapi.security.jwt;

import com.gohan.springrestapi.security.CustomUserDetailsService;
import com.gohan.springrestapi.security.util.SecurityCipherUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    @Value("${api-auth.cookie.access-token-cookie-name}")
    private String accessTokenCookieName;

    @Value("${api-auth.cookie.refresh-token-cookie-name}")
    private String refreshTokenCookieName;

    private final JwtTokenUtil jwtTokenUtil;

    private final CustomUserDetailsService userDetailsService;

    private final AccountStatusUserDetailsChecker accountStatusChecker;

    public JwtTokenAuthenticationFilter(JwtTokenUtil jwtTokenUtil, CustomUserDetailsService userDetailsService, AccountStatusUserDetailsChecker accountStatusChecker) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.accountStatusChecker = accountStatusChecker;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtToken(request, true);
            if (StringUtils.hasText(jwt)) {
                if (jwtTokenUtil.validateToken(jwt)) {
                    String username = jwtTokenUtil.getUsernameFromToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    accountStatusChecker.check(userDetails);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.addHeader("Message", "Invalid Token");
                }
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            //ex.printStackTrace();
            //response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ") && bearerToken.length() > 7) {
            String accessToken = bearerToken.substring(7);
            return SecurityCipherUtil.decrypt(accessToken);
        }
        return null;
    }

    private String getJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (accessTokenCookieName.equals(cookie.getName())) {
                String accessToken = cookie.getValue();
                if (accessToken == null) return null;

                return SecurityCipherUtil.decrypt(accessToken);
            }
        }
        return null;
    }

    private String getJwtToken(HttpServletRequest request, boolean fromCookie) {
        if (fromCookie) return getJwtFromCookie(request);

        return getJwtFromRequest(request);
    }
}
