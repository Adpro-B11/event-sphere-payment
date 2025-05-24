package id.ac.ui.cs.advprog.eventspherepayment.security;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final id.ac.ui.cs.advprog.eventspherepayment.security.JwtService jwtService;

    public JwtAuthenticationFilter(id.ac.ui.cs.advprog.eventspherepayment.security.JwtService jwtService) {
        this.jwtService = jwtService;
    }

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class); // Add logger instance

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        logger.debug("Processing request: {}", request.getRequestURI());
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            logger.debug("Extracted JWT: {}", token);

            try {
                if (jwtService.validateToken(token)) {
                    logger.debug("JWT validation successful.");
                    String userId = jwtService.getUserIdFromJWT(token);
                    String role = jwtService.getRoleFromJWT(token);
                    logger.debug("User ID from JWT: {}, Role from JWT: {}", userId, role);

                    SimpleGrantedAuthority authority;
                    if (role != null && !role.isEmpty()) {
                        if (role.startsWith("ROLE_")) {
                            authority = new SimpleGrantedAuthority(role.toUpperCase());
                        } else {
                            authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                        }
                    } else {
                        logger.warn("Role claim is missing or empty in JWT for user {}. Denying access.", userId);
                        filterChain.doFilter(request, response);
                        return;
                    }
                    List<SimpleGrantedAuthority> authorities = List.of(authority);
                    logger.debug("Authorities created: {}", authorities);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, token, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    logger.debug("Authentication set in SecurityContext for user: {}", userId);
                } else {
                    logger.warn("JWT validation failed. Token: {}", token);
                }
            } catch (JwtException e) {
                logger.error("Error processing JWT: {}. Token: {}", e.getMessage(), token);
            } catch (Exception e) {
                logger.error("Unexpected error during JWT processing: {}. Token: {}", e.getMessage(), token, e);
            }
        } else {
            logger.debug("No Bearer token found in Authorization header.");
        }

        filterChain.doFilter(request, response);
    }
}
