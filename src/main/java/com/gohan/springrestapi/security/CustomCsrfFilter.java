package com.gohan.springrestapi.security;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CustomCsrfFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = new HashSet<>(Arrays.asList("GET", "HEAD", "TRACE", "OPTIONS"));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (csrfTokenIsRequired(request)) {
            System.out.println(CsrfToken.class.getName());
            CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

            if (csrf != null) {
                Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
                String token = csrf.getToken();

                if (cookie == null || token != null && !token.equals(cookie.getValue())) {
                    System.out.println("token:  " + token);
                    cookie = new Cookie("XSRF-TOKEN", token);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean csrfTokenIsRequired(HttpServletRequest request) {
        return !SAFE_METHODS.contains(request.getMethod());
    }
}
