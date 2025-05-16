package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.service;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.factory.TransactionFactoryProducer;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.*;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserAccessStrategyServiceTest {

    private TransactionRepository transactionRepo;
    private TransactionFactoryProducer factoryProducer;
    private UserAccessStrategy strategy;
    private final String TEST_USER_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        transactionRepo = mock(TransactionRepository.class);
        factoryProducer = mock(TransactionFactoryProducer.class);
        strategy = new UserAccessStrategy(transactionRepo, factoryProducer);
    }

    @Test
    void testCreateTransaction_ThrowsUnsupportedOperation() {
        Transaction mockTransaction = mock(Transaction.class);

        assertThrows(
                UnsupportedOperationException.class,
                () -> strategy.createTransaction(mockTransaction),
                "User should not be able to create transactions directly"
        );
    }

    @Test
    void testCreateAndProcessTopUpTransaction_CallsRepoAndValidates() {
        String type = "TOPUP_BALANCE";
        String transactionId = "txn-123";
        String userId = UUID.randomUUID().toString();
        double amount = 500000;
        String method = "BANK_TRANSFER";
        Map<String, String> data = Map.of("accountNumber", "1234567890", "bankName", "BCA");

        Transaction mockTrx = mock(TopUpTransaction.class);
        when(transactionRepo.createAndSave(eq(type), eq(transactionId), eq(userId), eq(amount), eq(method), eq(data)))
                .thenReturn(mockTrx);

        Transaction result = strategy.createAndProcessTransaction(type, transactionId, userId, amount, method, data);

        verify(transactionRepo).createAndSave(type, transactionId, userId, amount, method, data);
        verify(mockTrx).validateTransaction();
        assertEquals(mockTrx, result);
    }

    @Test
    void testCreateAndProcessTicketPurchaseTransaction_CallsRepoAndValidates() {
        String type = "TICKET_PURCHASE";
        String transactionId = "txn-456";
        String userId = UUID.randomUUID().toString();
        double amount = 750000;
        String method = null;
        Map<String, String> data = Map.of("VIP", "2", "Regular", "3");

        Transaction mockTrx = mock(TicketPurchaseTransaction.class);
        when(transactionRepo.createAndSave(eq(type), eq(transactionId), eq(userId), eq(amount), eq(method), eq(data)))
                .thenReturn(mockTrx);

        Transaction result = strategy.createAndProcessTransaction(type, transactionId, userId, amount, method, data);

        verify(transactionRepo).createAndSave(type, transactionId, userId, amount, method, data);
        verify(mockTrx).validateTransaction();
        assertEquals(mockTrx, result);
    }

    @Test
    void testDeleteTransaction_ThrowsUnsupportedOperation() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> strategy.deleteTransaction("txn-123"),
                "User should not be able to delete transactions"
        );
    }

    @Test
    void testViewAllTransactions_ThrowsUnsupportedOperation() {
        assertThrows(
                UnsupportedOperationException.class,
                strategy::viewAllTransactions,
                "User should not be able to view all transactions"
        );
    }

    @Test
    void testFilterTransactions_ThrowsUnsupportedOperation() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> strategy.filterTransactions(TEST_USER_ID, "SUCCESS", "TICKET_PURCHASE"),
                "User should not be able to filter others transaction transactions"
        );
    }

    @Test
    void testViewUserTransactions_ReturnsOnlyUserTransactions() {
        // Setup
        Transaction userTrx1 = mock(Transaction.class);
        when(userTrx1.getUserId()).thenReturn(TEST_USER_ID);

        Transaction userTrx2 = mock(Transaction.class);
        when(userTrx2.getUserId()).thenReturn(TEST_USER_ID);

        Transaction otherUserTrx = mock(Transaction.class);
        when(otherUserTrx.getUserId()).thenReturn("other-user");

        List<Transaction> allTransactions = List.of(userTrx1, otherUserTrx, userTrx2);
        when(transactionRepo.findAll()).thenReturn(allTransactions);

        // Execute
        List<Transaction> userTransactions = strategy.viewUserTransactions(TEST_USER_ID);

        // Verify
        assertEquals(2, userTransactions.size());
        assertTrue(userTransactions.contains(userTrx1));
        assertTrue(userTransactions.contains(userTrx2));
        assertFalse(userTransactions.contains(otherUserTrx));
        verify(transactionRepo).findAll();
    }

    @Test
    void testViewUserTransactions_WithEmptyUserId_ThrowsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> strategy.viewUserTransactions(""),
                "Should throw IllegalArgumentException when userId is empty"
        );
    }

    @Test
    void testViewUserTransactions_WithNullUserId_ThrowsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> strategy.viewUserTransactions(null),
                "Should throw IllegalArgumentException when userId is null"
        );
    }
}
