package id.ac.ui.cs.advprog.eventspherepayment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper mapper;

    private String userId;
    private String adminId;

    @BeforeEach
    void setUp() {
        // userId dan adminId sudah diinisialisasi sebagai String dari UUID
        userId = UUID.randomUUID().toString();
        adminId = UUID.randomUUID().toString();
    }

    // --- TOP-UP ENDPOINT ---
    @Test
    void whenTopUpSuccess_thenReturnsCreatedAndId() throws Exception {
        UUID transactionId = UUID.randomUUID(); // Membuat UUID baru
        Transaction tx = mock(Transaction.class);
        when(tx.getTransactionId()).thenReturn(transactionId); 
        when(transactionService.createTopUpTransaction(eq(userId), eq(100.0), eq("CREDIT_CARD"), anyMap()))
                .thenReturn(tx);

        String json = mapper.writeValueAsString(Map.of(
                "userId", userId,
                "amount", 100.0,
                "method", "CREDIT_CARD",
                "paymentData", Map.of()
        ));

        mockMvc.perform(post("/api/transactions/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(transactionId.toString()));

        verify(transactionService).createTopUpTransaction(userId, 100.0, "CREDIT_CARD", anyMap());
    }

    @Test
    void whenTopUpInvalidAmount_thenReturnsBadRequest() throws Exception {
        when(transactionService.createTopUpTransaction(anyString(), eq(-50.0), anyString(), anyMap()))
                .thenThrow(new IllegalArgumentException("Invalid amount"));

        String json = mapper.writeValueAsString(Map.of(
                "userId", userId,
                "amount", -50.0,
                "method", "BANK_TRANSFER"
        ));

        mockMvc.perform(post("/api/transactions/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid amount"));
    }

    @Test
    void whenTopUpMissingMethod_thenReturnsBadRequest() throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "userId", userId,
                "amount", 50.0
        ));

        mockMvc.perform(post("/api/transactions/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // --- PURCHASE ENDPOINT ---
    @Test
    void whenPurchaseSuccess_thenReturnsCreatedAndId() throws Exception {
        UUID transactionId = UUID.randomUUID();
        Transaction tx = mock(Transaction.class);
        when(tx.getTransactionId()).thenReturn(transactionId); 
        when(transactionService.createTicketPurchaseTransaction(eq(userId), eq(75.0), anyMap()))
                .thenReturn(tx);

        String json = mapper.writeValueAsString(Map.of(
                "userId", userId,
                "amount", 75.0,
                "ticketData", Map.of("VIP","1")
        ));

        mockMvc.perform(post("/api/transactions/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(transactionId.toString())); // Membandingkan dengan String dari UUID

        verify(transactionService).createTicketPurchaseTransaction(userId, 75.0, anyMap());
    }

    @Test
    void whenPurchaseInsufficient_thenReturnsBadRequest() throws Exception {
        when(transactionService.createTicketPurchaseTransaction(anyString(), anyDouble(), anyMap()))
                .thenThrow(new IllegalStateException("Insufficient balance"));

        String json = mapper.writeValueAsString(Map.of(
                "userId", userId,
                "amount", 500.0,
                "ticketData", Map.of("VIP","1")
        ));

        mockMvc.perform(post("/api/transactions/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient balance"));
    }

    @Test
    void whenPurchaseMissingData_thenReturnsBadRequest() throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "userId", userId,
                "amount", 30.0
        ));

        mockMvc.perform(post("/api/transactions/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // --- GET BY ID ---
    @Test
    void whenGetByIdFound_thenReturnsOk() throws Exception {
        UUID txId = UUID.randomUUID(); 
        Transaction tx = mock(Transaction.class);
        when(tx.getTransactionId()).thenReturn(txId); 
        when(transactionService.getTransactionById(eq(txId.toString()), eq(userId), eq(false))) 
                .thenReturn(Optional.of(tx));

        mockMvc.perform(get("/api/transactions/{id}", txId.toString()) 
                        .param("currentUserId", userId)
                        .param("isAdmin", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(txId.toString())); // Membandingkan dengan String dari UUID
    }

    @Test
    void whenGetByIdNotFound_thenReturnsNotFound() throws Exception {
        String txId = UUID.randomUUID().toString(); 
        when(transactionService.getTransactionById(eq(txId), eq(userId), eq(false)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/{id}", txId)
                        .param("currentUserId", userId)
                        .param("isAdmin", "false"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetByIdAdmin_thenReturnsOk() throws Exception {
        UUID txId = UUID.randomUUID(); 
        Transaction tx = mock(Transaction.class);
        when(transactionService.getTransactionById(eq(txId.toString()), eq(adminId), eq(true))) 
                .thenReturn(Optional.of(tx));

        mockMvc.perform(get("/api/transactions/{id}", txId.toString()) 
                        .param("currentUserId", adminId)
                        .param("isAdmin", "true"))
                .andExpect(status().isOk());
    }

    // --- LIST/FILTER ---
    @Test
    void whenListUser_thenReturnsOk() throws Exception {
        when(transactionService.filterTransactions(eq(userId), eq(false), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/transactions")
                        .param("currentUserId", userId)
                        .param("isAdmin", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void whenListAdminWithFilters_thenReturnsOk() throws Exception {
        when(transactionService.filterTransactions(eq(null), eq(true), eq("SUCCESS"), eq("TOPUP_BALANCE"), eq("BANK_TRANSFER"), any(), any()))
                .thenReturn(List.of(mock(Transaction.class)));

        mockMvc.perform(get("/api/transactions")
                        .param("currentUserId", adminId)
                        .param("isAdmin", "true")
                        .param("status", "SUCCESS")
                        .param("type", "TOPUP_BALANCE")
                        .param("method", "BANK_TRANSFER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // --- DELETE ---
    @Test
    void whenDeleteAdmin_thenReturnsNoContent() throws Exception {
        String txId = UUID.randomUUID().toString(); 
        doNothing().when(transactionService).deleteTransaction(eq(txId), eq(true));

        mockMvc.perform(delete("/api/transactions/{id}", txId)
                        .param("currentUserId", adminId)
                        .param("isAdmin", "true"))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenDeleteUser_thenReturnsForbidden() throws Exception {
        String txId = UUID.randomUUID().toString(); 
        mockMvc.perform(delete("/api/transactions/{id}", txId)
                        .param("currentUserId", userId)
                        .param("isAdmin", "false"))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenDeleteNotFound_thenReturnsBadRequest() throws Exception {
        String txId = UUID.randomUUID().toString(); 
        doThrow(new java.util.NoSuchElementException("Not found"))
                .when(transactionService).deleteTransaction(eq(txId), eq(true));

        mockMvc.perform(delete("/api/transactions/{id}", txId)
                        .param("currentUserId", adminId)
                        .param("isAdmin", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Not found"));
    }
}