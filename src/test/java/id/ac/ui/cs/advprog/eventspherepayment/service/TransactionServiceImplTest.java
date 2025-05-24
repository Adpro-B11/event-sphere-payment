package id.ac.ui.cs.advprog.eventspherepayment.service;

import id.ac.ui.cs.advprog.eventspherepayment.client.AuthServiceClient;
import id.ac.ui.cs.advprog.eventspherepayment.enums.TransactionStatus;
import id.ac.ui.cs.advprog.eventspherepayment.enums.TransactionType;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.repository.TransactionRepository;
import id.ac.ui.cs.advprog.eventspherepayment.strategy.AccessStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock private TransactionRepository repository;
    @Mock private AuthServiceClient authClient;
    @Mock private RestTemplate restTemplate;
    private TransactionServiceImpl service;

    private String userId;
    private String adminId;
    private double amount;
    private String method;
    private Map<String, String> data;
    private AccessStrategy accessStrategy;

    @BeforeEach
    void setUp() {
        // Clear any existing authentication
        SecurityContextHolder.clearContext();

        String dummy = "http://localhost:8080/";
        service = new TransactionServiceImpl(repository, authClient, restTemplate, dummy);

        userId  = UUID.randomUUID().toString();
        adminId = UUID.randomUUID().toString();
        amount  = 1000.0;
        method  = "BANK_TRANSFER";
        data    = Map.of("bankName", "BCA", "accountNumber", "1234567890");

        // Default authenticate as regular user
        UsernamePasswordAuthenticationToken userAuth =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("USER"))
                );
        SecurityContextHolder.getContext().setAuthentication(userAuth);
        service.initStrategy();
    }

    @Test
    void createTicketPurchaseTransaction_CallAuthSync() {
        Transaction tx = mock(Transaction.class);
        Map<String, String> ticketData = data;

        when(repository.createAndSave(
                eq(TransactionType.TICKET_PURCHASE.getValue()),
                anyString(),
                eq(userId),
                eq(amount),
                anyString(),
                eq(ticketData)
        )).thenReturn(tx);
        when(repository.update(tx)).thenReturn(tx);

        Transaction result = service.createTicketPurchaseTransaction(userId, amount, data);

        assertSame(tx, result);

        InOrder inOrder = inOrder(repository, tx);
        inOrder.verify(repository).createAndSave(
                eq(TransactionType.TICKET_PURCHASE.getValue()),
                anyString(),
                eq(userId),
                eq(amount),
                anyString(),
                eq(ticketData)
        );
        inOrder.verify(tx).setStatus(TransactionStatus.PENDING.getValue());
        inOrder.verify(repository).update(tx);
    }

    @Test
    void processTopUpAsync_WhenAuthSucceeds_ShouldSetSuccess() throws Exception {
        Transaction tx = mock(Transaction.class);

        when(tx.getUserId()).thenReturn(UUID.fromString(userId));
        when(tx.getAmount()).thenReturn(amount);

        when(authClient.addBalance(userId, amount)).thenReturn(true);
        when(repository.update(tx)).thenReturn(tx);

        service.processTopUpAsync(tx).get();

        InOrder inOrder = inOrder(tx, repository, restTemplate);
        inOrder.verify(tx).setStatus(TransactionStatus.SUCCESS.getValue());
        inOrder.verify(repository).update(tx);

    }

    @Test
    void processPurchaseAsync_WhenAuthFails_ShouldSetFailedAndNotInvokeCallback() throws Exception {
        Transaction tx = mock(Transaction.class);

        when(tx.getUserId()).thenReturn(UUID.fromString(userId));
        when(tx.getAmount()).thenReturn(amount);
        when(authClient.deductBalance(userId, amount)).thenReturn(false);
        when(repository.update(tx)).thenReturn(tx);

        service.processPurchaseAsync(tx).get();

        InOrder inOrder = inOrder(tx, repository);
        inOrder.verify(tx).setStatus(TransactionStatus.FAILED.getValue());
        inOrder.verify(repository).update(tx);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void deleteTransaction_AsUser_ShouldThrowException() {
        UnsupportedOperationException ex = assertThrows(
                UnsupportedOperationException.class,
                () -> service.deleteTransaction(UUID.randomUUID().toString())
        );
        assertEquals("User cannot delete transactions.", ex.getMessage());
    }

    @Test
    void deleteTransaction_AsAdmin_ShouldCallRepositoryDelete() {
        // set admin authentication
        UsernamePasswordAuthenticationToken adminAuth =
                new UsernamePasswordAuthenticationToken(
                        adminId,
                        null,
                        List.of(new SimpleGrantedAuthority("ADMIN"))
                );
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        service.initStrategy();
        service.deleteTransaction("tx-1");
        verify(repository).deleteById("tx-1");
    }
}
