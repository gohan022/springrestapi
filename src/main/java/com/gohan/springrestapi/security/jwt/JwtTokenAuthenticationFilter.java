package com.gohan.springrestapi.security.jwt;

import com.gohan.springrestapi.security.CustomUserDetailsService;
import com.gohan.springrestapi.security.SessionService;
import com.gohan.springrestapi.security.TokenUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    @Value("${api-auth.jwt.header-string}")
    private String tokenHeader;

    @Value("${api-auth.jwt.token-prefix}")
    private String tokenPrefix;

    private String jwtToken;

    private static final String[] NEW_TOKEN_IGNORE = {"/auth/**", "/register"};

    private final JwtTokenUtil jwtTokenUtil;

    private final CustomUserDetailsService userDetailsService;

    private final AccountStatusUserDetailsChecker accountStatusChecker;

    private final SessionService sessionService;

    public JwtTokenAuthenticationFilter(JwtTokenUtil jwtTokenUtil,
                                        CustomUserDetailsService userDetailsService,
                                        AccountStatusUserDetailsChecker accountStatusChecker,
                                        SessionService sessionService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.accountStatusChecker = accountStatusChecker;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String username = this.getUsernameFromToken(request);

            if (username != null) {
                TokenUserDetails userDetails = (TokenUserDetails) userDetailsService.loadUserByUsername(username);

                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    accountStatusChecker.check(userDetails);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Check token in DB session if previous access token is not present throw error
                    if (Arrays.stream(NEW_TOKEN_IGNORE).noneMatch(e -> new AntPathMatcher().match(e, request.getRequestURI()))) {
                        if (!this.sessionService.isValid(request, this.jwtToken, (userDetails).getUser())) {
                            response.addHeader("X-AUTHENTICATION", "INVALID_SESSION");
                            throw new Exception("INVALID_SESSION");
                        }
                    }
                }
            } else {
                throw new Exception("INVALID TOKEN!");
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
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
