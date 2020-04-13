package com.gohan.springrestapi.security.jwt.resource;

import com.gohan.springrestapi.security.UserDetailsServiceImpl;
import com.gohan.springrestapi.security.jwt.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Objects;

@RestController
public class JwtAuthenticationRestController {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${api.jwt.header-string}")
    private String tokenHeader;

    @Value("${api.jwt.token.prefix}")
    private String tokenPrefix;

    public JwtAuthenticationRestController(UserDetailsServiceImpl userDetailsService, JwtTokenUtil jwtTokenUtil, AuthenticationManager authenticationManager) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("${api.jwt.token.uri}")
    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody JwtAuthenticateRequest authenticateRequest)
            throws JwtAuthenticationException {

        this.authenticate(authenticateRequest.getUsername(), authenticateRequest.getPassword());

        final UserDetails userDetails = this.userDetailsService.loadUserByUsername(authenticateRequest.getUsername());

        final String token = this.jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtAuthenticateResponse(token));
    }

    @GetMapping("${api.jwt.token.refresh}")
    public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request) {
        // System.out.println(request);
        String authToken = request.getHeader(tokenHeader);
        final String token = authToken.substring(tokenPrefix.length() + 1);
        String username = this.jwtTokenUtil.getUsernameFromToken(token);
        UserDetails user = this.userDetailsService.loadUserByUsername(username);

        if (user != null && jwtTokenUtil.canTokenBeRefreshed(token)) {
            String refreshedToken = jwtTokenUtil.refreshToken(token);
            return ResponseEntity.ok(new JwtAuthenticateResponse(refreshedToken));
        } else {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @ExceptionHandler({JwtAuthenticationException.class})
    public ResponseEntity<String> handleAuthenticationException(JwtAuthenticationException e) {
        // System.out.println(e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    private void authenticate(String username, String password) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        try {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            this.authenticationManager.authenticate(usernamePasswordAuthenticationToken);
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        } catch (DisabledException e) {
            System.out.println(e);
            throw new JwtAuthenticationException("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new JwtAuthenticationException("INVALID_CREDENTIALS", e);
        }

    }
}
