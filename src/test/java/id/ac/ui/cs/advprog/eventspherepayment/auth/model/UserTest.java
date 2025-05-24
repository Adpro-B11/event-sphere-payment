package id.ac.ui.cs.advprog.eventspherepayment.auth.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private final UUID userId = UUID.randomUUID();
    private final String username = "testuser";
    private final String email = "test@example.com";
    private final String phoneNumber = "1234567890";
    private final Role role = Role.ATTENDEE;
    private final BigDecimal balance = new BigDecimal("100.00");

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(userId)
                .username(username)
                .email(email)
                .phoneNumber(phoneNumber)
                .role(role)
                .balance(balance)
                .build();
    }

    @Test
    void testUserGetters() {
        assertEquals(userId, user.getId());
        assertEquals(username, user.getDisplayName());
        assertEquals(email, user.getEmail());
        assertEquals(phoneNumber, user.getPhoneNumber());
        assertEquals(role, user.getRole());
        assertEquals(balance, user.getBalance());
    }

    @Test
    void testGetAuthorities() {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

    @Test
    void testGetPassword() {
        assertNull(user.getPassword());
    }

    @Test
    void testGetUsername_returnsEmail() {
        assertEquals(email, user.getUsername());
    }

    @Test
    void testIsAccountNonExpired() {
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked() {
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired() {
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled() {
        assertTrue(user.isEnabled());
    }

    @Test
    void testUserBuilder() {
        User builtUser = User.builder()
                .id(userId)
                .username("anotheruser")
                .email("another@example.com")
                .phoneNumber("0987654321")
                .role(Role.ADMIN)
                .balance(new BigDecimal("200.50"))
                .build();

        assertEquals(userId, builtUser.getId());
        assertEquals("anotheruser", builtUser.getDisplayName());
        assertEquals("another@example.com", builtUser.getEmail());
        assertEquals("0987654321", builtUser.getPhoneNumber());
        assertEquals(Role.ADMIN, builtUser.getRole());
        assertEquals(new BigDecimal("200.50"), builtUser.getBalance());
    }

    @Test
    void testUserNoArgsConstructor() {
        User noArgUser = new User();
        assertNull(noArgUser.getId());
        assertNull(noArgUser.getUsername());
    }

    @Test
    void testUserAllArgsConstructor() {
        UUID newId = UUID.randomUUID();
        User allArgUser = new User(newId, "allArgsUser", "allargs@example.com", "1122334455", Role.ORGANIZER, BigDecimal.TEN);
        assertEquals(newId, allArgUser.getId());
        assertEquals("allArgsUser", allArgUser.getDisplayName());
        assertEquals("allargs@example.com", allArgUser.getEmail());
        assertEquals(Role.ORGANIZER, allArgUser.getRole());
    }

    @Test
    void testGetAuthoritiesWithAdminRole() {
        user = User.builder().role(Role.ADMIN).build();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void testGetAuthoritiesWithOrganizerRole() {
        user = User.builder().role(Role.ORGANIZER).build();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ORGANIZER")));
    }
}
