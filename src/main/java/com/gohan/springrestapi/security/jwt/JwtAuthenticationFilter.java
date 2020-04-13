package com.gohan.springrestapi.security.jwt;

import com.gohan.springrestapi.security.UserDetailsServiceImpl;
import com.gohan.springrestapi.security.jwt.resource.JwtAuthenticationException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private String jwtToken;

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final AccountStatusUserDetailsChecker accountStatusUserDetailsChecker;

    @Value("${api.jwt.header-string}")
    private String tokenHeader;

    @Value("${api.jwt.token.prefix}")
    private String tokenPrefix;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserDetailsServiceImpl userDetailsService, AccountStatusUserDetailsChecker accountStatusUserDetailsChecker) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.accountStatusUserDetailsChecker = accountStatusUserDetailsChecker;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
           // System.out.println("Jwt Authentication Filter...");
            String username = getUsernameFromToken(request);

            if (username != null) {
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                    if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        this.accountStatusUserDetailsChecker.check(userDetails);

                        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    } else {
                        logger.error("Jwt token cannot validate the user details with the token.");
                        throw new JwtTokenException("Invalid token!");
                    }
                } else {
                    logger.error("There is already a authentication present in security context.");
                    throw new JwtTokenException("Invalid token!");
                }
            }
        } catch (JwtException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (AccountStatusException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

        chain.doFilter(request, response); // next()
    }

    private String getUsernameFromToken(HttpServletRequest request) {
        String requestTokenHeader = request.getHeader(this.tokenHeader);
        if (requestTokenHeader != null && requestTokenHeader.startsWith(this.tokenPrefix + ' ')) {
            requestTokenHeader = requestTokenHeader.trim();

            if (!StringUtils.startsWithIgnoreCase(requestTokenHeader, this.tokenPrefix)) {
                return null;
            } else {
                this.jwtToken = requestTokenHeader.substring(this.tokenPrefix.length() + 1);
                return jwtTokenUtil.getUsernameFromToken(this.jwtToken);
            }
        } else {
            return null;
        }
    }
}
