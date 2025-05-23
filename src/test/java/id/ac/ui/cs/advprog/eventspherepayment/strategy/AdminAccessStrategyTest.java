package id.ac.ui.cs.advprog.eventsphere.payment_balance.strategy;

import id.ac.ui.cs.advprog.eventsphere.payment_balance.model.Transaction;
import id.ac.ui.cs.advprog.eventsphere.payment_balance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminAccessStrategyTest {

    private TransactionRepository repository;
    private AdminAccessStrategy strategy;
    private UUID txId;
    private String txIdStr;
    private String status, type, method;
    private LocalDateTime after, before;

    @BeforeEach
    void setUp() {
        repository = mock(TransactionRepository.class);
        strategy = new AdminAccessStrategy(repository);
        txId = UUID.randomUUID();
        txIdStr = txId.toString();
        status = "SUCCESS";
        type = "TOPUP_BALANCE";
        method = "BANK_TRANSFER";
        after = LocalDateTime.now().minusDays(1);
        before = LocalDateTime.now();
    }

    @Test
    void findById_delegatesToRepository() {
        Transaction tx = mock(Transaction.class);
        when(repository.findById(txIdStr)).thenReturn(Optional.of(tx));

        Optional<Transaction> result = strategy.findById(txIdStr);

        assertTrue(result.isPresent());
        assertSame(tx, result.get());
        verify(repository).findById(txIdStr);
    }

    @Test
    void viewAllTransactions_delegatesToRepository() {
        List<Transaction> txList = Arrays.asList(mock(Transaction.class), mock(Transaction.class));
        when(repository.findAll()).thenReturn(txList);

        List<Transaction> result = strategy.viewAllTransactions();

        assertEquals(txList, result);
        verify(repository).findAll();
    }

    @Test
    void viewUserTransactions_throwsUnsupported() {
        assertThrows(UnsupportedOperationException.class,
                () -> strategy.viewUserTransactions("any-user"));
    }

    @Test
    void filterTransactions_delegatesToRepositoryWithCorrectArgs() {
        List<Transaction> txList = Collections.singletonList(mock(Transaction.class));
        when(repository.findByFilters(txIdStr, status, type, method, after, before))
                .thenReturn(txList);

        List<Transaction> result = strategy.filterTransactions(
                txIdStr, status, type, method, after, before);

        assertEquals(txList, result);
        verify(repository).findByFilters(txIdStr, status, type, method, after, before);
    }

    @Test
    void deleteTransaction_delegatesToRepository() {
        strategy.deleteTransaction(txIdStr);
        verify(repository).deleteById(txIdStr);
    }
}


