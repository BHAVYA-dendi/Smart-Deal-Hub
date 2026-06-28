package com.smartdealhub.smartdealhub.security;

import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;

    public JwtAuthenticationFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (JwtUtil.validateToken(token)) {
                String email = JwtUtil.getEmailFromToken(token);
                User user = userService.getUserByEmail(email);
                // set user in request attribute for use in controllers
                request.setAttribute("currentUser", user);
            }
        }

        filterChain.doFilter(request, response);
    }
}