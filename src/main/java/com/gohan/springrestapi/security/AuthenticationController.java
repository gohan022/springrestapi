package com.gohan.springrestapi.security;

import com.gohan.springrestapi.security.dto.LoginRequest;
import com.gohan.springrestapi.security.dto.LoginResponse;
import com.gohan.springrestapi.security.dto.Token;
import com.gohan.springrestapi.security.dto.TokenUserDetails;
import com.gohan.springrestapi.security.jwt.JwtTokenUtil;
import com.gohan.springrestapi.security.util.CookieUtil;
import com.gohan.springrestapi.security.util.SecurityCipherUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class AuthenticationController {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationManager authenticationManager;
    private final CookieUtil cookieUtil;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthenticationController(AuthenticationManager authenticationManager, CookieUtil cookieUtil, CustomUserDetailsService userDetailsService, JwtTokenUtil jwtTokenUtil) {
        this.authenticationManager = authenticationManager;
        this.cookieUtil = cookieUtil;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Transactional
    @PostMapping("${api-auth.uri}")
    public ResponseEntity<LoginResponse> login(
            @CookieValue(name = "${api-auth.cookie.access-token-cookie-name}", required = false) String accessToken,
            @CookieValue(name = "${api-auth.cookie.refresh-token-cookie-name}", required = false) String refreshToken,
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            final TokenUserDetails user = (TokenUserDetails) authentication.getPrincipal();

            Boolean accessTokenValid = jwtTokenUtil.validateToken(SecurityCipherUtil.decrypt(accessToken));
            Boolean refreshTokenValid = jwtTokenUtil.validateToken(SecurityCipherUtil.decrypt(refreshToken));

            HttpHeaders responseHeaders = new HttpHeaders();
            Token newAccessToken;
            Token newRefreshToken;

            if ((!accessTokenValid && !refreshTokenValid) || (accessTokenValid && refreshTokenValid)) {
                newAccessToken = jwtTokenUtil.generateAccessToken(user);
                newRefreshToken = jwtTokenUtil.generateRefreshToken(user);
                addAccessTokenCookie(responseHeaders, newAccessToken);
                addRefreshTokenCookie(responseHeaders, newRefreshToken);
            }

            if (!accessTokenValid && refreshTokenValid) {
                newAccessToken = jwtTokenUtil.generateAccessToken(user);
                addAccessTokenCookie(responseHeaders, newAccessToken);
            }

            if (accessTokenValid && !refreshTokenValid) {
                newRefreshToken = jwtTokenUtil.generateRefreshToken(user);
                addRefreshTokenCookie(responseHeaders, newRefreshToken);
            }

            LoginResponse loginResponse = new LoginResponse(LoginResponse.SuccessFailure.SUCCESS, "Auth successful. Tokens are created in cookie.");
            return ResponseEntity.ok().headers(responseHeaders).body(loginResponse);

        } catch (DisabledException e) {
            throw new TokenAuthenticationException("USER_DISABLED", e);
        } catch (TokenAuthenticationException e) {
            throw new TokenAuthenticationException(e.getMessage());
        } catch (AuthenticationException e) {
            logger.warn("Authentication Exception: " + e.getMessage());
            throw new TokenAuthenticationException("INVALID_CREDENTIALS", e);
        }
    }

    @Transactional
    @PostMapping("${api-auth.refresh-uri}")
    public ResponseEntity<LoginResponse> refreshToken(@CookieValue(name = "${api-auth.cookie.refresh-token-cookie-name}", required = false) String refreshToken, Authentication authentication) {
        try {
            refreshToken = SecurityCipherUtil.decrypt(refreshToken);

            boolean refreshTokenValid = jwtTokenUtil.validateToken(refreshToken);
            if (!refreshTokenValid) {
                logger.warn("INVALID_REFRESH_TOKEN");
                LoginResponse loginResponse = new LoginResponse(LoginResponse.SuccessFailure.FAILURE, "INVALID_REFRESH_TOKEN");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(loginResponse);
            }

            TokenUserDetails user;

            if (authentication != null && authentication.isAuthenticated()) {
                user = (TokenUserDetails) authentication.getPrincipal();
            } else {
                String currentUser = jwtTokenUtil.getUsernameFromToken(refreshToken);
                user = (TokenUserDetails) userDetailsService.loadUserByUsername(currentUser);
            }

            Token newAccessToken = jwtTokenUtil.generateAccessToken(user);
            HttpHeaders responseHeaders = new HttpHeaders();
            addAccessTokenCookie(responseHeaders, newAccessToken);

            LoginResponse loginResponse = new LoginResponse(LoginResponse.SuccessFailure.SUCCESS, "Auth successful. Tokens are created in cookie.");
            return ResponseEntity.ok().headers(responseHeaders).body(loginResponse);

        } catch (DisabledException e) {
            throw new TokenAuthenticationException("USER_DISABLED", e);
        } catch (TokenAuthenticationException e) {
            throw new TokenAuthenticationException(e.getMessage());
        } catch (AuthenticationException e) {
            logger.warn("Authentication Exception: " + e.getMessage());
            throw new TokenAuthenticationException("INVALID_CREDENTIALS", e);
        }
    }

  /*  @GetMapping("/logout")
    public ResponseEntity<String> logOut(HttpServletRequest request, HttpServletResponse response){
        if (request.getCookies() != null) {
            System.out.println("i found some cookies");
            for (Cookie cookie : request.getCookies()) {
                cookie.setMaxAge(0);
                cookie.setValue("");
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }

        SecurityContextHolder.clearContext();
        return new ResponseEntity (new ApiResponseMessage(true, userService.logout(request, response)), HttpStatus.OK);
    }*/

    @ExceptionHandler(TokenAuthenticationException.class)
    public ResponseEntity<LoginResponse> handleAuthenticationException(TokenAuthenticationException e) {
        LoginResponse loginResponse = new LoginResponse(LoginResponse.SuccessFailure.FAILURE, e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(loginResponse);
    }

    private void addAccessTokenCookie(HttpHeaders httpHeaders, Token token) {
        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(token.getTokenValue(), token.getDuration()).toString());
    }

    private void addRefreshTokenCookie(HttpHeaders httpHeaders, Token token) {
        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(token.getTokenValue(), token.getDuration()).toString());
    }
}
