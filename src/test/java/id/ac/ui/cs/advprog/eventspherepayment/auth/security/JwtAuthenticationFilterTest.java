package id.ac.ui.cs.advprog.eventspherepayment.auth.security;

import id.ac.ui.cs.advprog.eventspherepayment.auth.model.User;
import id.ac.ui.cs.advprog.eventspherepayment.auth.model.Role;
import id.ac.ui.cs.advprog.eventspherepayment.auth.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


import java.io.IOException;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User testUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .role(Role.ATTENDEE)
                .build();
    }

    @Test
    void doFilterInternal_whenNoAuthorizationHeader_shouldContinueFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).isTokenValid(anyString());
        verify(userService, never()).getUserByToken(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_whenAuthorizationHeaderDoesNotStartWithBearer_shouldContinueFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidTokenFormat");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).isTokenValid(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_whenTokenIsInvalid_shouldContinueFilterChain() throws ServletException, IOException {
        String jwt = "invalid-jwt";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.isTokenValid(jwt)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, times(1)).isTokenValid(jwt);
        verify(userService, never()).getUserByToken(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_whenTokenIsValidAndUserFound_shouldSetAuthentication() throws ServletException, IOException {
        String jwt = "valid-jwt";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.isTokenValid(jwt)).thenReturn(true);
        when(userService.getUserByToken(jwt)).thenReturn(testUser);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, times(1)).isTokenValid(jwt);
        verify(userService, times(1)).getUserByToken(jwt);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(testUser, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().containsAll(testUser.getAuthorities()));
    }

    @Test
    void doFilterInternal_whenTokenIsValidButUserNotFound_shouldNotSetAuthentication() throws ServletException, IOException {
        String jwt = "valid-jwt-no-user";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.isTokenValid(jwt)).thenReturn(true);
        when(userService.getUserByToken(jwt)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, times(1)).isTokenValid(jwt);
        verify(userService, times(1)).getUserByToken(jwt);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_whenAuthenticationAlreadyExists_shouldNotReAuthenticate() throws ServletException, IOException {
        String jwt = "valid-jwt";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("previousUser", null));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(userService, never()).getUserByToken(jwt);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("previousUser", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void doFilterInternal_whenTokenIsValidAndUserFound_andAuthIsNull_shouldSetAuthentication() throws ServletException, IOException {
        String jwt = "valid-jwt-for-auth";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.isTokenValid(jwt)).thenReturn(true);
        when(userService.getUserByToken(jwt)).thenReturn(testUser);
        SecurityContextHolder.getContext().setAuthentication(null);


        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).isTokenValid(jwt);
        verify(userService).getUserByToken(jwt);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(testUser, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
