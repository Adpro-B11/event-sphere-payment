package id.ac.ui.cs.advprog.eventspherepayment.security;

import java.io.IOException;
import java.util.List;

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

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                if (jwtService.validateToken(token)) {
                    String userId = jwtService.extractUserId(token);
                    String role = jwtService.getRoleFromJWT(token);

                    SimpleGrantedAuthority authority;
                    if (role != null && !role.isEmpty()) {
                        if (role.startsWith("ROLE_")) {
                            authority = new SimpleGrantedAuthority(role.toUpperCase());
                        } else {
                            authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                        }
                    } else {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    List<SimpleGrantedAuthority> authorities = List.of(authority);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, token, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException e) {
                // Do nothing, token invalid
            } catch (Exception e) {
                // Do nothing, unexpected error
            }
        }

        filterChain.doFilter(request, response);
    }
}
