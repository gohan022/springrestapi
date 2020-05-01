package com.gohan.springrestapi.security;

import com.gohan.springrestapi.security.jwt.JwtTokenUtil;
import com.google.gson.Gson;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
public class AuthenticationController {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final SessionService sessionService;

    public AuthenticationController(AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService,
                                    JwtTokenUtil jwtTokenUtil, SessionService sessionService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.sessionService = sessionService;
    }

    @Transactional
    @PostMapping("${api-auth.uri}")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest loginRequest, HttpServletRequest request
    ) {
        try {

            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            final TokenUserDetails user = (TokenUserDetails) authentication.getPrincipal();

            HttpHeaders responseHeaders = new HttpHeaders();
            String newAccessToken;
            String newRefreshToken = null;

            newAccessToken = jwtTokenUtil.generateAccessToken(user);

            if (loginRequest.isRememberMe()) {
                newRefreshToken = jwtTokenUtil.generateRefreshToken(user);
            }

            this.sessionService.updatePayload(request, newAccessToken, user.getUser());

            LoginResponse loginResponse = new LoginResponse(LoginResponse.SuccessFailure.SUCCESS, "Auth successful");
            loginResponse.setAccessToken(newAccessToken);
            loginResponse.setRefreshToken(newRefreshToken);
            return ResponseEntity.ok().headers(responseHeaders).body(loginResponse);

        } catch (DisabledException e) {
            throw new TokenAuthenticationException("USER_DISABLED", e);
        } catch (Exception e) {
            logger.warn("Authentication Exception: " + e.getMessage());
            throw new TokenAuthenticationException("INVALID_CREDENTIALS", e);
        }
    }

    @Transactional
    @PostMapping("${api-auth.refresh-uri}")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshRequest refreshRequest, HttpServletRequest request) {
        String refreshToken = refreshRequest.getToken();

        try {

            String currentUser = jwtTokenUtil.getUsernameFromToken(refreshToken);
            if(currentUser == null) {
                throw new TokenAuthenticationException("INVALID_CREDENTIALS");
            }

            TokenUserDetails userDetails = (TokenUserDetails) userDetailsService.loadUserByUsername(currentUser);
            jwtTokenUtil.validateToken(refreshToken, userDetails);
            String newAccessToken = jwtTokenUtil.generateAccessToken(userDetails);

            this.sessionService.updatePayload(request, newAccessToken, userDetails.getUser());

            LoginResponse loginResponse = new LoginResponse(LoginResponse.SuccessFailure.SUCCESS, "Auth successful.");
            loginResponse.setAccessToken(newAccessToken);
            return ResponseEntity.status(HttpStatus.OK).body(loginResponse);

        } catch (DisabledException e) {
            throw new TokenAuthenticationException("USER_DISABLED", e);
        } catch (Exception e) {
            logger.warn("Authentication Exception: " + e.getMessage());
            throw new TokenAuthenticationException("INVALID_CREDENTIALS", e);
        }
    }

    @Transactional
    @PostMapping("${api-auth.destroy-uri}")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        if (authentication != null && authentication.isAuthenticated()) {
            this.sessionService.clearSession(request, ((TokenUserDetails) authentication.getPrincipal()).getUser());
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                cookie.setMaxAge(0);
                cookie.setValue("");
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }

        SecurityContextHolder.clearContext();

        return new ResponseEntity<>(new Gson().toJson("LOGOUT_SUCCESS"), HttpStatus.OK);
    }

    @ExceptionHandler(TokenAuthenticationException.class)
    public ResponseEntity<LoginResponse> handleAuthenticationException(TokenAuthenticationException e) {
        LoginResponse loginResponse = new LoginResponse(LoginResponse.SuccessFailure.FAILURE, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(loginResponse);
    }
}

@Getter
@NoArgsConstructor
class LoginRequest {
    private String username;
    private String password;
    private boolean rememberMe;
}

@Getter
@NoArgsConstructor
class RefreshRequest {
    String token;
}

@Data
@RequiredArgsConstructor
class LoginResponse {
    @NonNull
    private SuccessFailure status;
    private String accessToken;
    private String refreshToken;
    @NonNull
    private String message;

    public enum SuccessFailure {
        SUCCESS, FAILURE
    }
}
