//package id.ac.ui.cs.advprog.eventspherepayment.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import id.ac.ui.cs.advprog.eventspherepayment.dto.PurchaseRequest;
//import id.ac.ui.cs.advprog.eventspherepayment.dto.TopUpRequest;
//import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
//import id.ac.ui.cs.advprog.eventspherepayment.security.JwtService;
//import id.ac.ui.cs.advprog.eventspherepayment.service.TransactionService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.context.annotation.Primary;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.*;
//import java.util.concurrent.CompletableFuture;
//
//import static org.hamcrest.Matchers.hasSize;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.http.HttpStatus.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(TransactionController.class)
//@Import(TransactionControllerTest.TestSecurityConfig.class)
//class TransactionControllerTest {
//
//    @TestConfiguration
//    @EnableWebSecurity
//    static class TestSecurityConfig {
//        @Bean
//        @Primary
//        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
//            return http
//                    .csrf(csrf -> csrf.disable())
//                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
//                    .build();
//        }
//    }
//
//    @Autowired
//    MockMvc mockMvc;
//
//    @MockBean
//    TransactionService service;
//
//    @MockBean
//    JwtService jwtService;
//
//    @Autowired
//    ObjectMapper mapper;
//
//    String userId;
//    String adminId;
//    String eventId;
//    String ticketId;
//    int quantity;
//
//    @BeforeEach
//    void setUp() {
//        userId = UUID.randomUUID().toString();
//        adminId = UUID.randomUUID().toString();
//        eventId = UUID.randomUUID().toString();
//        ticketId = UUID.randomUUID().toString();
//        quantity = 3;
//        setupSecurityContext(userId, false);
//    }
//
//    private void setupSecurityContext(String userId, boolean isAdmin) {
//        // 1. Mock Authentication
//        Authentication auth = mock(Authentication.class);
//        doReturn(userId).when(auth).getName();
//
//        // 2. Siapkan authorities dengan tipe wildcard
//        Collection<? extends GrantedAuthority> authorities =
//                isAdmin
//                        ? List.of(new SimpleGrantedAuthority("ADMIN"))
//                        : List.of(new SimpleGrantedAuthority("USER"));
//        doReturn(authorities).when(auth).getAuthorities();
//
//        // 3. Mock SecurityContext dan pasang ke holder
//        SecurityContext ctx = mock(SecurityContext.class);
//        doReturn(auth).when(ctx).getAuthentication();
//        SecurityContextHolder.setContext(ctx);
//    }
//
//
//    @Test
//    void topUpFailed_Returns400NegativeAmount() throws Exception {
//        // Given
//        when(service.createTopUpTransaction(anyString(), anyDouble(), anyString(), anyMap()))
//                .thenThrow(new ResponseStatusException(BAD_REQUEST));
//
//        TopUpRequest req = new TopUpRequest(userId, -100.0, "CREDIT_CARD", Map.of());
//
//        // When & Then
//        mockMvc.perform(post("/api/transactions/topup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(req)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void purchaseFailed_Returns400() throws Exception {
//
//        // Given
//        when(service.createTicketPurchaseTransaction(anyString(),anyString(), anyDouble(), anyMap()))
//                .thenThrow(new ResponseStatusException(BAD_REQUEST));
//
//        PurchaseRequest req = new PurchaseRequest(userId,eventId, -10.0, quantity,ticketId);
//
//        // When & Then
//        mockMvc.perform(post("/api/transactions/purchase")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(req)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void getByIdFound_Returns200() throws Exception {
//        // Given
//        UUID txId = UUID.randomUUID();
//        Transaction tx = mock(Transaction.class);
//        when(service.getTransactionById(txId.toString()))
//                .thenReturn(CompletableFuture.completedFuture(Optional.of(tx)));
//
//        // When & Then
//        mockMvc.perform(get("/api/transactions/{id}", txId))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void deleteTransaction_Returns200() throws Exception {
//        // Given
//        String txId = UUID.randomUUID().toString();
//        when(service.deleteTransaction(txId))
//                .thenReturn(CompletableFuture.completedFuture(null));
//
//        // When & Then
//        mockMvc.perform(delete("/api/transactions/{id}", txId))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void deleteTransactionNotFound_Returns400() throws Exception {
//        // Given
//        String txId = UUID.randomUUID().toString();
//        when(service.deleteTransaction(txId))
//                .thenThrow(new ResponseStatusException(BAD_REQUEST, "Not found"));
//
//        // When & Then
//        mockMvc.perform(delete("/api/transactions/{id}", txId))
//                .andExpect(status().isBadRequest())
//                .andExpect(status().reason("Not found"));
//    }
//}