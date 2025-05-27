package id.ac.ui.cs.advprog.eventspherepayment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.eventspherepayment.dto.PurchaseRequest;
import id.ac.ui.cs.advprog.eventspherepayment.dto.TopUpRequest;
import id.ac.ui.cs.advprog.eventspherepayment.dto.TransactionResponse;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.security.JwtService;
import id.ac.ui.cs.advprog.eventspherepayment.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(TransactionControllerTest.TestSecurityConfig.class)
class TransactionControllerTest {

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        @Primary
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TransactionService service;

    @MockBean
    JwtService jwtService;

    @Autowired
    ObjectMapper mapper;

    String userId;
    String adminId;
    String eventId;
    String ticketId;
    int quantity;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        adminId = UUID.randomUUID().toString();
        eventId = UUID.randomUUID().toString();
        ticketId = UUID.randomUUID().toString();
        quantity = 3;
        setupSecurityContext(userId, false);
    }

    private void setupSecurityContext(String userId, boolean isAdmin) {
        Authentication auth = mock(Authentication.class);
        doReturn(userId).when(auth).getName();

        Collection<? extends GrantedAuthority> authorities =
                isAdmin
                        ? List.of(new SimpleGrantedAuthority("ADMIN"))
                        : List.of(new SimpleGrantedAuthority("USER"));
        doReturn(authorities).when(auth).getAuthorities();

        SecurityContext ctx = mock(SecurityContext.class);
        doReturn(auth).when(ctx).getAuthentication();
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    void purchaseFailed_Returns400_NegativeAmount() throws Exception {
        // Given - negative amount should be rejected by controller validation
        PurchaseRequest req = new PurchaseRequest(userId, eventId, -10.0, quantity, ticketId);

        // When & Then
        mockMvc.perform(post("/api/transactions/purchase/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        // Service should not be called due to validation failure
        verify(service, never()).createTicketPurchaseTransaction(anyString(), anyString(), anyDouble(), anyMap());
    }

    @Test
    void purchaseFailed_Returns400_NegativeQuantity() throws Exception {
        // Given - negative quantity should be rejected by controller validation
        PurchaseRequest req = new PurchaseRequest(userId, eventId, 100.0, -1, ticketId);

        // When & Then
        mockMvc.perform(post("/api/transactions/purchase/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createTicketPurchaseTransaction(anyString(), anyString(), anyDouble(), anyMap());
    }

    @Test
    void purchaseFailed_Returns400_EmptyTicketData() throws Exception {
        // Given - create request with empty ticket data
        PurchaseRequest req = new PurchaseRequest();
        req.setUserId(userId);
        req.setEventId(eventId);
        req.setAmount(100.0);
        req.setTicketData(Map.of()); // empty map

        // When & Then
        mockMvc.perform(post("/api/transactions/purchase/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createTicketPurchaseTransaction(anyString(), anyString(), anyDouble(), anyMap());
    }

    @Test
    void getByIdFound_Returns200() throws Exception {
        // Given
        String txId = UUID.randomUUID().toString();
        TransactionResponse mockResponse = mock(TransactionResponse.class);

        when(service.initStrategy()).thenReturn(userId);
        when(service.getTransactionById(txId))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(mockResponse)));

        // When & Then
        mockMvc.perform(get("/api/transactions/{id}", txId))
                .andExpect(status().isOk());

        verify(service).initStrategy();
        verify(service).getTransactionById(txId);
    }

    @Test
    void deleteTransaction_Returns200() throws Exception {
        // Given
        String txId = UUID.randomUUID().toString();
        when(service.deleteTransaction(txId))
                .thenReturn(CompletableFuture.completedFuture(null));

        // When & Then
        mockMvc.perform(delete("/api/transactions/{id}", txId))
                .andExpect(status().isOk());

        verify(service).deleteTransaction(txId);
    }

