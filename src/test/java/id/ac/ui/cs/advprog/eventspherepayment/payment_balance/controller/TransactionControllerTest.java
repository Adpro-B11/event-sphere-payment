package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.controller;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.TransactionStatus;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.TransactionType;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.TicketPurchaseTransaction;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.TopUpTransaction;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.service.AdminAccessStrategy;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.service.TransactionService;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.service.UserAccessStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private AdminAccessStrategy adminAccessStrategy;

    @MockBean
    private UserAccessStrategy userAccessStrategy;

    private final String USER_ID = UUID.randomUUID().toString();
    private final String ADMIN_ID = "admin-123";

    @BeforeEach
    public void setup() {
        reset(transactionService, adminAccessStrategy, userAccessStrategy);
    }

    // Stub void setStrategy(...) since it returns void
    private void mockUserAuthentication() {
        doNothing().when(transactionService).setStrategy(any());
    }

    private void mockAdminAuthentication() {
        doNothing().when(transactionService).setStrategy(any());
    }

    @Test
    public void testTopUpBalance_Success() throws Exception {
        String requestBody = """
                {
                    "amount": 100.0,
                    "method": "CREDIT_CARD",
                    "data": {
                        "cardNumber": "1234-5678-9012-3456"
                    }
                }
                """;

        mockMvc.perform(post("/api/transactions/topup")
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.transactionId", not(emptyString())));

        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<String> methodCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> dataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(transactionService).setStrategy(eq(userAccessStrategy));
        verify(transactionService).createTransaction(
                typeCaptor.capture(),
                anyString(),
                userIdCaptor.capture(),
                amountCaptor.capture(),
                methodCaptor.capture(),
                dataCaptor.capture()
        );

        assertEquals(TransactionType.TOPUP_BALANCE.name(), typeCaptor.getValue());
        assertEquals(USER_ID, userIdCaptor.getValue());
        assertEquals(100.0, amountCaptor.getValue());
        assertEquals("CREDIT_CARD", methodCaptor.getValue());
        assertEquals("1234-5678-9012-3456", dataCaptor.getValue().get("cardNumber"));
    }

    @Test
    public void testTopUpBalance_InvalidAmount() throws Exception {
        String requestBody = """
                {
                    "amount": -100.0,
                    "method": "CREDIT_CARD"
                }
                """;

        doThrow(new IllegalArgumentException("Amount must be positive"))
                .when(transactionService).createTransaction(anyString(), anyString(), anyString(), eq(-100.0), anyString(), anyMap());

        mockMvc.perform(post("/api/transactions/topup")
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Amount must be positive")));

        verify(transactionService).setStrategy(eq(userAccessStrategy));
    }

    @Test
    public void testPurchaseTicket_Success() throws Exception {
        mockUserAuthentication();

        String requestBody = """
        {
            "amount": 50.0,
            "eventId": "event-123"
        }
        """;

        mockMvc.perform(post("/api/transactions/purchase-ticket")
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.transactionId", not(emptyString())));

        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Map> dataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(transactionService).setStrategy(eq(userAccessStrategy));
        verify(transactionService).createTransaction(
                typeCaptor.capture(),
                anyString(),
                userIdCaptor.capture(),
                amountCaptor.capture(),
                eq("BALANCE"),
                dataCaptor.capture()
        );

        assertEquals(TransactionType.TICKET_PURCHASE.name(), typeCaptor.getValue());
        assertEquals(USER_ID, userIdCaptor.getValue());
        assertEquals(50.0, amountCaptor.getValue());
        assertEquals("event-123", dataCaptor.getValue().get("eventId"));
    }

    @Test
    public void testPurchaseTicket_InsufficientBalance() throws Exception {
        mockUserAuthentication();

        String requestBody = """
        {
            "amount": 500.0,
            "eventId": "event-123"
        }
        """;

        doThrow(new IllegalStateException("Insufficient balance for ticket purchase"))
                .when(transactionService).createTransaction(anyString(), anyString(), anyString(), anyDouble(), anyString(), anyMap());

        mockMvc.perform(post("/api/transactions/purchase-ticket")
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Insufficient balance")));
    }

    @Test
    public void testPurchaseTicket_MissingEventId() throws Exception {
        mockUserAuthentication();

        String requestBody = """
        {
            "amount": 50.0
        }
        """;

        mockMvc.perform(post("/api/transactions/purchase-ticket")
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    private List<Transaction> getMockTransactions() {
        List<Transaction> mockTransactions = new ArrayList<>();

        // TopUpTransaction
        Map<String, String> paymentData = new HashMap<>();
        paymentData.put("accountNumber", "1234567890123456");
        Transaction topUp = new TopUpTransaction(
                "trx-123",
                USER_ID,
                TransactionType.TOPUP_BALANCE.name(),
                "CREDIT_CARD",
                100.0,
                paymentData
        );
        topUp.setStatus(TransactionStatus.SUCCESS.name());

        // TicketPurchaseTransaction
        Map<String, String> ticketData = new HashMap<>();
        ticketData.put("event-123", "2");
        Transaction purchase = new TicketPurchaseTransaction(
                "trx-456",
                USER_ID,
                TransactionType.TICKET_PURCHASE.name(),
                50.0,
                ticketData
        );
        purchase.setStatus(TransactionStatus.SUCCESS.name());

        mockTransactions.add(topUp);
        mockTransactions.add(purchase);
        return mockTransactions;
    }

    @Test
    public void testGetUserTransactions_Success() throws Exception {
        mockUserAuthentication();
        List<Transaction> mockTransactions = getMockTransactions();

        when(transactionService.viewUserTransactions(USER_ID)).thenReturn(mockTransactions);

        mockMvc.perform(get("/api/transactions/user")
                        .header("User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type", is(TransactionType.TOPUP_BALANCE.name())))
                .andExpect(jsonPath("$[0].status", is(TransactionStatus.SUCCESS.name())))
                .andExpect(jsonPath("$[0].amount", is(100.0)))
                .andExpect(jsonPath("$[1].type", is(TransactionType.TICKET_PURCHASE.name())))
                .andExpect(jsonPath("$[1].amount", is(50.0)));

        verify(transactionService).setStrategy(eq(userAccessStrategy));
        verify(transactionService).viewUserTransactions(USER_ID);
    }

    @Test
    public void testFilterUserTransactionsByType_Success() throws Exception {
        mockUserAuthentication();

        // Ambil semua transaksi mock
        List<Transaction> allTransactions = getMockTransactions();

        // Filter hanya transaksi TOPUP_BALANCE
        List<Transaction> filteredTransactions = allTransactions.stream()
                .filter(t -> t.getType().equals(TransactionType.TOPUP_BALANCE.name()))
                .collect(Collectors.toList());

        when(transactionService.filterTransactionsByType(USER_ID, TransactionType.TOPUP_BALANCE.name()))
                .thenReturn(filteredTransactions);

        mockMvc.perform(get("/api/transactions/user/filter")
                        .header("User-Id", USER_ID)
                        .param("type", TransactionType.TOPUP_BALANCE.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].transactionId", is("trx-123")))
                .andExpect(jsonPath("$[0].type", is(TransactionType.TOPUP_BALANCE.name())))
                .andExpect(jsonPath("$[0].status", is(TransactionStatus.SUCCESS.name())))
                .andExpect(jsonPath("$[0].amount", is(100.0)))
                .andExpect(jsonPath("$[0].method", is("CREDIT_CARD")))
                .andExpect(jsonPath("$[0].paymentData.accountNumber", is("1234567890123456")));

        verify(transactionService).setStrategy(eq(userAccessStrategy));
        verify(transactionService).filterTransactionsByType(USER_ID, TransactionType.TOPUP_BALANCE.name());
    }

    @Test
    public void testFilterUserTransactionsByType_EmptyResults() throws Exception {
        mockUserAuthentication();

        List<Transaction> emptyTransactions = new ArrayList<>();

        when(transactionService.filterTransactionsByType(USER_ID, TransactionType.TICKET_PURCHASE.name()))
                .thenReturn(emptyTransactions);

        mockMvc.perform(get("/api/transactions/user/filter")
                        .header("User-Id", USER_ID)
                        .param("type", TransactionType.TICKET_PURCHASE.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(transactionService).setStrategy(eq(userAccessStrategy));
        verify(transactionService).filterTransactionsByType(USER_ID, TransactionType.TICKET_PURCHASE.name());
    }

    @Test
    public void testGetAllTransactions_AdminSuccess() throws Exception {
        mockAdminAuthentication();
        List<Transaction> mockTransactions = getMockTransactions();

        when(transactionService.viewAllTransactions()).thenReturn(mockTransactions);

        mockMvc.perform(get("/api/transactions/all")
                        .header("User-Id", ADMIN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type", is(TransactionType.TOPUP_BALANCE.name())))
                .andExpect(jsonPath("$[0].status", is(TransactionStatus.SUCCESS.name())))
                .andExpect(jsonPath("$[0].amount", is(100.0)))
                .andExpect(jsonPath("$[1].type", is(TransactionType.TICKET_PURCHASE.name())))
                .andExpect(jsonPath("$[1].status", is(TransactionStatus.SUCCESS.name())))
                .andExpect(jsonPath("$[1].amount", is(50.0)));

        verify(transactionService).setStrategy(eq(adminAccessStrategy));
        verify(transactionService).viewAllTransactions();
    }


    @Test
    public void testGetAllTransactions_UserForbidden() throws Exception {
        mockUserAuthentication();

        mockMvc.perform(get("/api/transactions/all")
                        .header("User-Id", USER_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Access denied")));

        verify(transactionService, never()).viewAllTransactions();
    }

    @Test
    public void testFilterTransactions_AdminSuccess() throws Exception {
        mockAdminAuthentication();
        List<Transaction> mockTransactions = getMockTransactions();

        when(transactionService.filterTransactions(USER_ID,
                TransactionStatus.SUCCESS.name(),
                TransactionType.TOPUP_BALANCE.name()))
                .thenReturn(mockTransactions);

        mockMvc.perform(get("/api/transactions/filter")
                        .header("User-Id", ADMIN_ID)
                        .param("userId", USER_ID)
                        .param("status", TransactionStatus.SUCCESS.name())
                        .param("type", TransactionType.TOPUP_BALANCE.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type", is(TransactionType.TOPUP_BALANCE.name())))
                .andExpect(jsonPath("$[0].status", is(TransactionStatus.SUCCESS.name())))
                .andExpect(jsonPath("$[0].amount", is(100.0)));

        verify(transactionService).setStrategy(eq(adminAccessStrategy));
        verify(transactionService).filterTransactions(USER_ID,
                TransactionStatus.SUCCESS.name(),
                TransactionType.TOPUP_BALANCE.name());
    }

    @Test
    public void testFilterTransactions_AdminWithPartialParameters() throws Exception {
        mockAdminAuthentication();
        List<Transaction> mockTransactions = getMockTransactions();

        when(transactionService.filterTransactions(null, null,
                TransactionType.TOPUP_BALANCE.name()))
                .thenReturn(mockTransactions);

        mockMvc.perform(get("/api/transactions/filter")
                        .header("User-Id", ADMIN_ID)
                        .param("type", TransactionType.TOPUP_BALANCE.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(transactionService).setStrategy(eq(adminAccessStrategy));
        verify(transactionService).filterTransactions(null, null,
                TransactionType.TOPUP_BALANCE.name());
    }

    @Test
    public void testFilterTransactions_UserForbidden() throws Exception {
        mockUserAuthentication();

        mockMvc.perform(get("/api/transactions/filter")
                        .header("User-Id", USER_ID)
                        .param("userId", USER_ID)
                        .param("status", TransactionStatus.SUCCESS.name())
                        .param("type", TransactionType.TOPUP_BALANCE.name()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Access denied")));

        verify(transactionService, never()).filterTransactions(anyString(), anyString(), anyString());
    }

    @Test
    public void testDeleteTransaction_AdminSuccess() throws Exception {
        mockAdminAuthentication();

        String transactionId = "transaction-123";

        mockMvc.perform(delete("/api/transactions/" + transactionId)
                        .header("User-Id", ADMIN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(transactionService).setStrategy(eq(adminAccessStrategy));
        verify(transactionService).deleteTransaction(transactionId);
    }

    @Test
    public void testDeleteTransaction_UserForbidden() throws Exception {
        mockUserAuthentication();

        String transactionId = "transaction-123";

        mockMvc.perform(delete("/api/transactions/" + transactionId)
                        .header("User-Id", USER_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Access denied")));

        verify(transactionService, never()).deleteTransaction(anyString());
    }

    @Test
    public void testDeleteTransaction_TransactionNotFound() throws Exception {
        mockAdminAuthentication();

        String transactionId = "nonexistent-transaction";

        doThrow(new NoSuchElementException("Transaction not found"))
                .when(transactionService).deleteTransaction(transactionId);

        mockMvc.perform(delete("/api/transactions/" + transactionId)
                        .header("User-Id", ADMIN_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Transaction not found")));

        verify(transactionService).setStrategy(eq(adminAccessStrategy));
        verify(transactionService).deleteTransaction(transactionId);
    }
}
