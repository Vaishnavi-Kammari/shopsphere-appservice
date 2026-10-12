package com.shopsphere.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads "Authorization: Bearer <token>", validates it and populates the Spring Security context.
 * Deliberately NOT a @Component: it is created inside SecurityConfig so it only runs in the security chain.
 */
public class TokenAuthFilter extends OncePerRequestFilter {
    private final TokenService tokens;
    private final DbUserDetailsService userDetailsService;

    public TokenAuthFilter(TokenService tokens, DbUserDetailsService userDetailsService) {
        this.tokens = tokens;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            Optional<String> email = tokens.parse(header.substring(7).trim());
            if (email.isPresent()) {
                try {
                    UserDetails user = userDetailsService.loadUserByUsername(email.get());
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            user.getUsername(), null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (UsernameNotFoundException ignored) {
                    // token for a user that no longer exists: treat as anonymous
                }
            }
        }
        chain.doFilter(request, response);
    }
}
