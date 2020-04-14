package com.gohan.springrestapi.security;

import com.gohan.springrestapi.security.dto.LoginRequest;
import com.gohan.springrestapi.security.dto.LoginResponse;
import com.gohan.springrestapi.security.dto.Token;
import com.gohan.springrestapi.security.dto.TokenUser;
import com.gohan.springrestapi.security.util.CookieUtil;
import com.gohan.springrestapi.security.util.SecurityCipherUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final CookieUtil cookieUtil;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticationController(AuthenticationManager authenticationManager, CookieUtil cookieUtil, CustomUserDetailsService userDetailsService, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.cookieUtil = cookieUtil;
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("${api-auth.uri}")
    public ResponseEntity<LoginResponse> login(
            @CookieValue(name = "accessToken", required = false) String accessToken,
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            final TokenUser user = (TokenUser) userDetailsService.loadUserByUsername(loginRequest.getUsername());

            Boolean accessTokenValid = jwtTokenProvider.validateToken(SecurityCipherUtil.decrypt(accessToken));
            Boolean refreshTokenValid = jwtTokenProvider.validateToken(SecurityCipherUtil.decrypt(refreshToken));

            HttpHeaders responseHeaders = new HttpHeaders();
            Token newAccessToken;
            Token newRefreshToken;

            if ((!accessTokenValid && !refreshTokenValid) || (accessTokenValid && refreshTokenValid)) {
                newAccessToken = jwtTokenProvider.generateAccessToken(user);
                newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
                addAccessTokenCookie(responseHeaders, newAccessToken);
                addRefreshTokenCookie(responseHeaders, newRefreshToken);
            }

            if (!accessTokenValid && refreshTokenValid) {
                newAccessToken = jwtTokenProvider.generateAccessToken(user);
                addAccessTokenCookie(responseHeaders, newAccessToken);
            }

            LoginResponse loginResponse = new LoginResponse(LoginResponse.SuccessFailure.SUCCESS, "Auth successful. Tokens are created in cookie.");
            return ResponseEntity.ok().headers(responseHeaders).body(loginResponse);

        } catch (DisabledException e) {
            System.out.println(e);
            throw new TokenAuthenticationException("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new TokenAuthenticationException("INVALID_CREDENTIALS", e);
        }
    }

    @PostMapping("${api-auth.refresh-uri}")
    public ResponseEntity<LoginResponse> refreshToken(
            @CookieValue(name = "accessToken", required = false) String accessToken,
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        try {
            accessToken = SecurityCipherUtil.decrypt(accessToken);
            refreshToken = SecurityCipherUtil.decrypt(refreshToken);

            boolean refreshTokenValid = jwtTokenProvider.validateToken(refreshToken);
            if (!refreshTokenValid) {
                throw new TokenAuthenticationException("Refresh Token is invalid!");
            }

            String currentUser = jwtTokenProvider.getUsernameFromToken(accessToken);

            TokenUser user = (TokenUser) userDetailsService.loadUserByUsername(currentUser);

            Token newAccessToken = jwtTokenProvider.generateAccessToken(user);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(newAccessToken.getTokenValue(), newAccessToken.getDuration()).toString());

            LoginResponse loginResponse = new LoginResponse(LoginResponse.SuccessFailure.SUCCESS, "Auth successful. Tokens are created in cookie.");
            return ResponseEntity.ok().headers(responseHeaders).body(loginResponse);
        } catch (DisabledException e) {
            System.out.println(e);
            throw new TokenAuthenticationException("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new TokenAuthenticationException("INVALID_CREDENTIALS", e);
        }
    }

    @ExceptionHandler({TokenAuthenticationException.class})
    public ResponseEntity<String> handleAuthenticationException(TokenAuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    private void addAccessTokenCookie(HttpHeaders httpHeaders, Token token) {
        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(token.getTokenValue(), token.getDuration()).toString());
    }

    private void addRefreshTokenCookie(HttpHeaders httpHeaders, Token token) {
        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(token.getTokenValue(), token.getDuration()).toString());
    }
}
