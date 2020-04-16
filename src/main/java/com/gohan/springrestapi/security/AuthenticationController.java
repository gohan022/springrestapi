package com.gohan.springrestapi.security;

import com.gohan.springrestapi.security.dto.LoginRequest;
import com.gohan.springrestapi.security.dto.LoginResponse;
import com.gohan.springrestapi.security.dto.Token;
import com.gohan.springrestapi.security.dto.TokenUserDetails;
import com.gohan.springrestapi.security.util.CookieUtil;
import com.gohan.springrestapi.security.jwt.JwtTokenUtil;
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
    private final JwtTokenUtil jwtTokenUtil;

    public AuthenticationController(AuthenticationManager authenticationManager, CookieUtil cookieUtil, CustomUserDetailsService userDetailsService, JwtTokenUtil jwtTokenUtil) {
        this.authenticationManager = authenticationManager;
        this.cookieUtil = cookieUtil;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("${api-auth.uri}")
    public ResponseEntity<LoginResponse> login(
            @CookieValue(name = "${api-auth.cookie.access-token-cookie-name}", required = false) String accessToken,
            @CookieValue(name = "${api-auth.cookie.refresh-token-cookie-name}", required = false) String refreshToken,
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            final TokenUserDetails user = (TokenUserDetails) userDetailsService.loadUserByUsername(loginRequest.getUsername());

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
            @CookieValue(name = "${api-auth.cookie.access-token-cookie-name}", required = false) String accessToken,
            @CookieValue(name = "${api-auth.cookie.refresh-token-cookie-name}", required = false) String refreshToken) {
        try {
            accessToken = SecurityCipherUtil.decrypt(accessToken);
            refreshToken = SecurityCipherUtil.decrypt(refreshToken);

            boolean refreshTokenValid = jwtTokenUtil.validateToken(refreshToken);
            if (!refreshTokenValid) {
                throw new TokenAuthenticationException("Refresh Token is invalid!");
            }

            String currentUser = jwtTokenUtil.getUsernameFromToken(accessToken);

            TokenUserDetails user = (TokenUserDetails) userDetailsService.loadUserByUsername(currentUser);

            Token newAccessToken = jwtTokenUtil.generateAccessToken(user);
            HttpHeaders responseHeaders = new HttpHeaders();
            addAccessTokenCookie(responseHeaders, newAccessToken);

            LoginResponse loginResponse = new LoginResponse(LoginResponse.SuccessFailure.SUCCESS, "Auth successful. Tokens are created in cookie.");
            return ResponseEntity.ok().headers(responseHeaders).body(loginResponse);

        } catch (DisabledException e) {
            System.out.println(e);
            throw new TokenAuthenticationException("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
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
