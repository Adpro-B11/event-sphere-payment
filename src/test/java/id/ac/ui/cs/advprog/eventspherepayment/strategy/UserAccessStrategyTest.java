package id.ac.ui.cs.advprog.eventsphere.payment_balance.strategy;

import id.ac.ui.cs.advprog.eventsphere.payment_balance.model.Transaction;
import id.ac.ui.cs.advprog.eventsphere.payment_balance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAccessStrategyTest {

    private TransactionRepository repository;
    private UserAccessStrategy strategy;
    private String currentUserIdStr;
    private LocalDateTime after, before;

    @BeforeEach
    void setUp() {
        repository = mock(TransactionRepository.class);
        UUID currentUserId = UUID.randomUUID();
        currentUserIdStr = currentUserId.toString();
        strategy = new UserAccessStrategy(repository, currentUserIdStr);
        after = LocalDateTime.now().minusDays(1);
        before = LocalDateTime.now();
    }

    @Test
    void findById_userOwnsTransaction_returnsTransaction() {
        UUID txId = UUID.randomUUID();
        String txIdStr = txId.toString();

        Transaction tx = mock(Transaction.class);
        when(tx.getUserId()).thenReturn(UUID.fromString(currentUserIdStr));
        when(repository.findById(txIdStr)).thenReturn(Optional.of(tx));

        Optional<Transaction> result = strategy.findById(txIdStr);

        assertTrue(result.isPresent());
        assertSame(tx, result.get());
        verify(repository).findById(txIdStr);
    }

    @Test
    void findById_userDoesNotOwnTransaction_returnsEmpty() {
        UUID txId = UUID.randomUUID();
        String txIdStr = txId.toString();

        Transaction tx = mock(Transaction.class);
        when(tx.getUserId()).thenReturn(UUID.randomUUID());
        when(repository.findById(txIdStr)).thenReturn(Optional.of(tx));

        Optional<Transaction> result = strategy.findById(txIdStr);

        assertTrue(result.isEmpty());
        verify(repository).findById(txIdStr);
    }

    @Test
    void findById_transactionNotFound_returnsEmpty() {
        String txIdStr = UUID.randomUUID().toString();
        when(repository.findById(txIdStr)).thenReturn(Optional.empty());

        Optional<Transaction> result = strategy.findById(txIdStr);

        assertTrue(result.isEmpty());
        verify(repository).findById(txIdStr);
    }


    @Test
    void viewAllTransactions_throwsUnsupported() {
        assertThrows(UnsupportedOperationException.class,
                () -> strategy.viewAllTransactions());
    }

    @Test
    void viewUserTransactions_delegatesToRepositoryWithCurrentUser() {
        List<Transaction> txList = Collections.singletonList(mock(Transaction.class));
        when(repository.findByFilters(currentUserIdStr, null, null, null, null, null))
                .thenReturn(txList);

        List<Transaction> result = strategy.viewUserTransactions("ignored");

        assertEquals(txList, result);
        verify(repository).findByFilters(currentUserIdStr, null, null, null, null, null);
    }

    @Test
    void filterTransactions_delegatesToRepositoryWithCurrentUser() {
        List<Transaction> txList = Collections.singletonList(mock(Transaction.class));
        when(repository.findByFilters(currentUserIdStr, "FAILED", "TICKET_PURCHASE", "IN_APP_BALANCE", after, before))
                .thenReturn(txList);

        List<Transaction> result = strategy.filterTransactions(
                currentUserIdStr, "FAILED", "TICKET_PURCHASE", "IN_APP_BALANCE", after, before);

        assertEquals(txList, result);
        verify(repository).findByFilters(currentUserIdStr, "FAILED", "TICKET_PURCHASE", "IN_APP_BALANCE", after, before);
    }

    @Test
    void deleteTransaction_throwsUnsupported() {
        assertThrows(UnsupportedOperationException.class,
                () -> strategy.deleteTransaction("tx-999"));
    }
}