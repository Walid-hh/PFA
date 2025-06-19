package com.covoituragedigitalise.trip.config;

import com.covoituragedigitalise.trip.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        System.out.println("🔍 JWT FILTER - URL: " + requestPath);
        System.out.println("🔍 JWT FILTER - Method: " + method);

        final String authHeader = request.getHeader("Authorization");
        System.out.println("🔍 JWT FILTER - Auth Header: " + authHeader);

        // Laisser passer les endpoints publics
        if (isPublicEndpoint(requestPath, method)) {
            System.out.println("🟢 JWT FILTER - Endpoint public, accès autorisé");
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ JWT FILTER - Pas de token Bearer trouvé");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        System.out.println("🔑 JWT FILTER - Token extrait: " + jwt.substring(0, Math.min(jwt.length(), 20)) + "...");

        try {
            userEmail = jwtService.extractUsername(jwt);
            System.out.println("📧 JWT FILTER - Email extrait: " + userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtService.isTokenValid(jwt, userEmail)) {
                    System.out.println("✅ JWT FILTER - Token valide");

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("🎯 JWT FILTER - Authentication définie dans SecurityContext");
                } else {
                    System.out.println("❌ JWT FILTER - Token invalide");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ JWT FILTER - Erreur lors du traitement du token: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path, String method) {
        // Endpoints publics (sans authentification)
        return path.contains("/health") ||
                (path.equals("/api/trips/search") && "GET".equals(method)) ||
                (path.startsWith("/api/trips/") && "GET".equals(method) && path.matches("/api/trips/\\d+"));
    }
}