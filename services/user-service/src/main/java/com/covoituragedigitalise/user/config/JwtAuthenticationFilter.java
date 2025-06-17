package com.covoituragedigitalise.user.config;

import com.covoituragedigitalise.user.service.CustomUserDetailsService;
import com.covoituragedigitalise.user.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        System.out.println("🔍 JWT FILTER - URL: " + request.getRequestURI());
        System.out.println("🔍 JWT FILTER - Method: " + request.getMethod());
        System.out.println("🔍 JWT FILTER - Auth Header: " + (authHeader != null ? authHeader.substring(0, Math.min(authHeader.length(), 30)) + "..." : "null"));

        // Si pas de header Authorization ou ne commence pas par Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ JWT FILTER - Pas de token Bearer trouvé");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extraire le token
            jwt = authHeader.substring(7);
            System.out.println("🔑 JWT FILTER - Token extrait: " + jwt.substring(0, Math.min(jwt.length(), 20)) + "...");

            // Extraire l'email du token
            userEmail = jwtService.extractUsername(jwt);
            System.out.println("📧 JWT FILTER - Email extrait: " + userEmail);

            // Si email extrait et pas encore authentifié
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("🔍 JWT FILTER - Chargement des détails utilisateur...");

                // Charger les détails de l'utilisateur
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                System.out.println("👤 JWT FILTER - UserDetails chargé: " + userDetails.getUsername());

                // Valider le token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    System.out.println("✅ JWT FILTER - Token valide");

                    // Créer l'authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Définir l'authentification dans le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("🎯 JWT FILTER - Authentication définie dans SecurityContext");
                } else {
                    System.out.println("❌ JWT FILTER - Token invalide");
                }
            }

        } catch (Exception e) {
            System.err.println("💥 JWT FILTER - Erreur: " + e.getMessage());
            e.printStackTrace();
        }

        // Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}