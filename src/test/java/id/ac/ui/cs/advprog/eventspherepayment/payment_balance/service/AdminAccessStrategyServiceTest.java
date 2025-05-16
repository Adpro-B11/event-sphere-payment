package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.service;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminAccessStrategyServiceTest {

    private TransactionRepository transactionRepo;
    private AdminAccessStrategy strategy;

    @BeforeEach
    void setUp() {
        transactionRepo = mock(TransactionRepository.class);
        strategy = new AdminAccessStrategy(transactionRepo);
    }

    @Test
    void testDeleteTransaction_CallsRepositoryDeleteOnce() {
        String transactionId = "trx-123";
        strategy.deleteTransaction(transactionId);
        verify(transactionRepo, times(1)).deleteById(transactionId);
    }

    @Test
    void testCreateTransaction_ThrowsUnsupportedOperationException() {
        Transaction mockTrx = mock(Transaction.class);
        UnsupportedOperationException ex = assertThrows(
                UnsupportedOperationException.class,
                () -> strategy.createTransaction(mockTrx)
        );
        assertEquals("Admin cannot create transactions", ex.getMessage());
    }

    @Test
    void testViewUserTransactions_ThrowsUnsupportedOperationException() {
        UnsupportedOperationException ex = assertThrows(
                UnsupportedOperationException.class,
                () -> strategy.viewUserTransactions("user-1")
        );
        assertEquals("Admin cannot view own transactions", ex.getMessage());
    }

    @Test
    void testViewAllTransactions_CallsFindAll() {
        strategy.viewAllTransactions();
        verify(transactionRepo, times(1)).findAll();
    }

    @Test
    void testViewAllTransactions_ReturnsCombinedList() {
        Transaction trx1 = mock(Transaction.class);
        Transaction trx2 = mock(Transaction.class);
        when(transactionRepo.findAll()).thenReturn(List.of(trx1, trx2));

        List<Transaction> result = strategy.viewAllTransactions();

        assertEquals(2, result.size());
        assertSame(trx1, result.get(0));
        assertSame(trx2, result.get(1));
    }

    @Test
    void testFilterTransactions_CallsFindByFilters() {
        String userId = UUID.randomUUID().toString();
        String status = "SUCCESS";
        String type = "TICKET_PURCHASE";

        strategy.filterTransactions(userId, status, type);

        verify(transactionRepo, times(1)).findByFilters(userId, status, type);
    }

    @Test
    void testFilterTransactions_ReturnsFilteredList() {
        String status = "TOPUP_BALANCE";
        Transaction failedTrx = mock(Transaction.class);
        when(transactionRepo.findByFilters(null, status, null)).thenReturn(List.of(failedTrx));

        List<Transaction> result = strategy.filterTransactions(null, status, null);

        assertEquals(1, result.size());
        assertSame(failedTrx, result.get(0));
    }

    @Test
    void testFilterTransactionsByType_CallsFindByFiltersWithNullStatus() {
        String userId = UUID.randomUUID().toString();
        String type = "TICKET_PURCHASE";

        strategy.filterTransactionsByType(userId, type);

        verify(transactionRepo, times(1)).findByFilters(userId, null, type);
    }

    @Test
    void testFilterTransactionsByType_ReturnsFilteredList() {
        String userId = UUID.randomUUID().toString();
        String type = "TOPUP_BALANCE";
        Transaction trx = mock(Transaction.class);
        when(transactionRepo.findByFilters(userId, null, type)).thenReturn(List.of(trx));

        List<Transaction> result = strategy.filterTransactionsByType(userId, type);

        assertEquals(1, result.size());
        assertSame(trx, result.get(0));
    }
}
