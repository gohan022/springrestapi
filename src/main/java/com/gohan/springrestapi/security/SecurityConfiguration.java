package com.gohan.springrestapi.security;

import com.gohan.springrestapi.security.jwt.JwtTokenAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true, // For method level security (determines if the @Secured annotation should be enabled
        jsr250Enabled = true, // allows us to use the @RoleAllowed annotation
        prePostEnabled = true // enables Spring Security pre/post annotations
)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String[] CSRF_IGNORE = {"/auth/**"};
    private static final String[] AUTH_IGNORE = {"/register", "/auth/**", "/error", "/actuator/**"};
    private static final String[] ROLE_USER_MATCHER = {"/user/**"};
    private static final String[] ROLE_ADMIN_MATCHER = {"/admin/**"};

    private final CustomUserDetailsService userDetailsService;
    private final TokenAuthenticationEntryPoint tokenAuthenticationEntryPoint;
    private final JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter;
    private final CustomCsrfFilter customCsrfFilter;

    public SecurityConfiguration(CustomUserDetailsService userDetailsService, TokenAuthenticationEntryPoint tokenAuthenticationEntryPoint,
                                 JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter, CustomCsrfFilter customCsrfFilter) {
        this.userDetailsService = userDetailsService;
        this.tokenAuthenticationEntryPoint = tokenAuthenticationEntryPoint;
        this.jwtTokenAuthenticationFilter = jwtTokenAuthenticationFilter;
        this.customCsrfFilter = customCsrfFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
                .and().exceptionHandling().authenticationEntryPoint(tokenAuthenticationEntryPoint)
                .and().formLogin().disable().httpBasic().disable();

        http.csrf()
                .ignoringAntMatchers(CSRF_IGNORE)
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .addFilterAfter(customCsrfFilter, CsrfFilter.class);

        http.authorizeRequests()
                .antMatchers(AUTH_IGNORE).permitAll()
                .antMatchers(ROLE_USER_MATCHER).hasAuthority("USER")
                .antMatchers(ROLE_ADMIN_MATCHER).hasAuthority("ADMIN")
                .anyRequest().authenticated()
                .and().logout().permitAll().logoutRequestMatcher(new AntPathRequestMatcher("/api/user/logout", "POST"));

        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().addFilterBefore(jwtTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        /*http.requiresChannel()
            .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
            .requiresSecure();*/

         http.headers().xssProtection();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoderBean());
    }

    @Bean
    public PasswordEncoder passwordEncoderBean() {
        return new BCryptPasswordEncoder();
    }

    @Override
    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods(GET.name(), POST.name(), PUT.name(), DELETE.name(), OPTIONS.name())
                        .allowedHeaders("*")
                        .allowCredentials(true)
                       // .exposedHeaders("X-AUTHENTICATION")
                        .maxAge(3600);
            }
        };
    }
}
