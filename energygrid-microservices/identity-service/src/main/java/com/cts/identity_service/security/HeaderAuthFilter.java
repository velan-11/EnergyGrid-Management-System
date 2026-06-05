package com.cts.identity_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Trusts identity headers injected by the upstream API gateway. The gateway
 * validates the JWT, so this service only reads the resulting X-Auth-* headers
 * and populates the SecurityContext with the user's email and granted role.
 */
@Component
public class HeaderAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String email = request.getHeader("X-Auth-Email");
        String role  = request.getHeader("X-Auth-Role");

        // Only authenticate when the gateway supplied both headers; otherwise the
        // request stays anonymous and is rejected by the authorization rules.
        if (email != null && !email.isBlank()
                && role != null && !role.isBlank()) {

            // Spring expects authorities prefixed with "ROLE_" for hasRole() checks.
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
