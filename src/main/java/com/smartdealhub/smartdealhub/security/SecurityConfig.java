package com.smartdealhub.smartdealhub.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.smartdealhub.smartdealhub.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for Postman testing
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/favicon.*",
                                "/api/users/login",
                                "/api/users/register",
                                "/api/users/register/storeowner",
                                "/api/users/register/admin"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/**",
                                "/api/offers/**",
                                "/api/group-deals/**",
                                "/api/reviews/**",
                                "/stores/**"
                        ).permitAll()
                        .requestMatchers("/api/owner/**").hasAnyRole("STORE_OWNER", "ADMIN")
                        .requestMatchers("/api/users/*/role", "/api/users/*/status", "/api/users/pending", "/api/users/*/approve")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .userDetailsService(userDetailsService)
                .httpBasic(Customizer.withDefaults()); // Optional for testing

        // Ensure JWT request attribute population for controllers
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}