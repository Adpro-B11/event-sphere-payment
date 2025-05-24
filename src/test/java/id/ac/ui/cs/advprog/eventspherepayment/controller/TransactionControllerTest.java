package id.ac.ui.cs.advprog.eventspherepayment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.eventspherepayment.dto.PurchaseRequest;
import id.ac.ui.cs.advprog.eventspherepayment.dto.TopUpRequest;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.security.JwtService;
import id.ac.ui.cs.advprog.eventspherepayment.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

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

    @BeforeEach
    void setUp() {
        userId  = UUID.randomUUID().toString();
        adminId = UUID.randomUUID().toString();
    }


    @Test
    void topUpSuccess_Returns200WithoutMessage() throws Exception {
        Transaction tx = mock(Transaction.class);
        when(service.createTopUpTransaction(
                eq(userId), eq(100.0), eq("CREDIT_CARD"), anyMap()))
                .thenReturn(tx);

        TopUpRequest req = new TopUpRequest(userId, 100.0, "CREDIT_CARD", Map.of());
        mockMvc.perform(post("/api/transactions/topup")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(service).createTopUpTransaction(eq(userId), eq(100.0),
                eq("CREDIT_CARD"), anyMap());
    }

    @Test
    void topUpFailed_Returns400NegativeAmount() throws Exception {
        when(service.createTopUpTransaction(anyString(), anyDouble(), anyString(), anyMap()))
                .thenThrow(new ResponseStatusException(BAD_REQUEST));

        TopUpRequest req = new TopUpRequest(userId, -100.0, "CREDIT_CARD", Map.of());
        mockMvc.perform(post("/api/transactions/topup")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void topUpMissingMethod() throws Exception {
        Map<String, Object> partial = Map.of(
                "userId", userId,
                "amount", 50.0,
                "paymentData", Map.of()
        );
        mockMvc.perform(post("/api/transactions/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(partial)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void purchaseSuccess_Returns200WithoutMessage() throws Exception {
        Transaction tx = mock(Transaction.class);
        when(service.createTicketPurchaseTransaction(
                eq(userId), eq(50.0), anyMap()))
                .thenReturn(tx);

        PurchaseRequest req = new PurchaseRequest(userId, 50.0, Map.of("VIP", "1"));
        mockMvc.perform(post("/api/transactions/purchase")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(service).createTicketPurchaseTransaction(eq(userId), eq(50.0), anyMap());
    }

    @Test
    void purchaseFailed_Returns400WithoutMessage() throws Exception {
        when(service.createTicketPurchaseTransaction(anyString(), anyDouble(), anyMap()))
                .thenThrow(new ResponseStatusException(BAD_REQUEST));

        PurchaseRequest req = new PurchaseRequest(userId, -10.0, Map.of());
        mockMvc.perform(post("/api/transactions/purchase")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void purchaseSuccess_CreatedWithBody() throws Exception {
        UUID txId = UUID.randomUUID();
        Transaction tx = mock(Transaction.class);
        when(tx.getTransactionId()).thenReturn(txId);
        when(service.createTicketPurchaseTransaction(
                eq(userId), eq(75.0), anyMap()))
                .thenReturn(tx);

        PurchaseRequest req = new PurchaseRequest(userId, 75.0, Map.of("VIP", "1"));
        mockMvc.perform(post("/api/transactions/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(txId.toString()));

        verify(service).createTicketPurchaseTransaction(eq(userId), eq(75.0), anyMap());
    }

    @Test
    void purchaseInsufficient() throws Exception {
        when(service.createTicketPurchaseTransaction(anyString(), anyDouble(), anyMap()))
                .thenThrow(new ResponseStatusException(BAD_REQUEST, "Insufficient balance"));

        PurchaseRequest req = new PurchaseRequest(userId, 500.0, Map.of("VIP", "1"));
        mockMvc.perform(post("/api/transactions/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Insufficient balance"));
    }

    @Test
    void purchaseMissingData() throws Exception {
        Map<String, Object> partial = Map.of(
                "userId", userId,
                "amount", 30.0
        );
        mockMvc.perform(post("/api/transactions/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(partial)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIdFound() throws Exception {
        UUID txId = UUID.randomUUID();
        Transaction tx = mock(Transaction.class);
        when(service.getTransactionById(txId.toString()))
                .thenReturn(Optional.of(tx));

        mockMvc.perform(get("/api/transactions/{id}", txId)
                        .param("currentUserId", userId)
                        .param("isAdmin", "false"))
                .andExpect(status().isOk());
    }

    @Test
    void getByIdNotFound() throws Exception {
        String txId = UUID.randomUUID().toString();
        when(service.getTransactionById(txId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/{id}", txId)
                        .param("currentUserId", userId)
                        .param("isAdmin", "false"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByIdAsAdmin() throws Exception {
        UUID txId = UUID.randomUUID();
        when(service.getTransactionById(txId.toString()))
                .thenReturn(Optional.of(mock(Transaction.class)));

        mockMvc.perform(get("/api/transactions/{id}", txId)
                        .param("currentUserId", adminId)
                        .param("isAdmin", "true"))
                .andExpect(status().isOk());
    }


    @Test
    void listUserNoFilters() throws Exception {
        when(service.filterTransactions(userId, false, null, null, null, null, null))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/transactions")
                        .param("currentUserId", userId)
                        .param("isAdmin", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void listAdminWithFilters() throws Exception {
        when(service.filterTransactions(null, true,
                "SUCCESS", "TOPUP_BALANCE",
                "BANK_TRANSFER", null, null))
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

    @Test
    void deleteAsAdminNoContent() throws Exception {
        String txId = UUID.randomUUID().toString();
        doNothing().when(service).deleteTransaction(txId);

        mockMvc.perform(delete("/api/transactions/{id}", txId)
                        .param("currentUserId", adminId)
                        .param("isAdmin", "true"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAsUserForbidden() throws Exception {
        String txId = UUID.randomUUID().toString();
        mockMvc.perform(delete("/api/transactions/{id}", txId)
                        .param("currentUserId", userId)
                        .param("isAdmin", "false"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteNotFoundBadRequest() throws Exception {
        String txId = UUID.randomUUID().toString();
        doThrow(new ResponseStatusException(BAD_REQUEST, "Not found"))
                .when(service).deleteTransaction(txId);

        mockMvc.perform(delete("/api/transactions/{id}", txId)
                        .param("currentUserId", adminId)
                        .param("isAdmin", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Not found"));
    }
}
