package id.ac.ui.cs.advprog.eventspherepayment.service;

import id.ac.ui.cs.advprog.eventspherepayment.enums.PaymentMethod;
import id.ac.ui.cs.advprog.eventspherepayment.enums.TransactionStatus;
import id.ac.ui.cs.advprog.eventspherepayment.enums.TransactionType;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private UserService userService;

    private TransactionServiceImpl service;

    private String userId;
    private double amount;
    private String method;
    private Map<String, String> data;

    @BeforeEach
    void setUp() {
        service = new TransactionServiceImpl(repository, userService);
        userId = UUID.randomUUID().toString();
        amount = 1000.0;
        method = PaymentMethod.BANK_TRANSFER.getValue();
        data = Map.of("bankName", "BCA", "accountNumber", "1234567890");
    }

    @Test
    void testTopUpBalance_Success_shouldCallAddBalance_andSetSuccess() {
        // arrange
        Transaction mockTx = mock(Transaction.class);
        when(repository.createAndSave(eq(TransactionType.TOPUP_BALANCE.getValue()),
                anyString(), eq(userId), eq(amount), eq(method), eq(data)))
                .thenReturn(mockTx);
        when(repository.update(mockTx)).thenReturn(mockTx);

        // act
        Transaction result = service.createTopUpTransaction(userId, amount, method, data);

        // assert
        verify(userService).addBalance(userId, amount);
        verify(mockTx).setStatus(TransactionStatus.SUCCESS.getValue());
        verify(repository).update(mockTx);
        assertSame(mockTx, result);
    }

    @Test
    void testTopUpBalance_FailureOnException_shouldSetFailed() {
        // arrange
        Transaction mockTx = mock(Transaction.class);
        when(repository.createAndSave(anyString(), anyString(), anyString(), anyDouble(), anyString(), anyMap()))
                .thenReturn(mockTx);
        when(repository.update(mockTx)).thenReturn(mockTx);
        doThrow(new RuntimeException("up error"))
                .when(userService).addBalance(userId, amount);

        // act
        Transaction result = service.createTopUpTransaction(userId, amount, method, data);

        // assert
        verify(userService).addBalance(userId, amount);
        verify(mockTx).setStatus(TransactionStatus.FAILED.getValue());
        verify(repository).update(mockTx);
        assertSame(mockTx, result);
    }

    @Test
    void testTicketPurchase_Success_shouldDeductBalance_andSetSuccess() {
        // arrange
        Transaction mockTx = mock(Transaction.class);
        when(repository.createAndSave(eq(TransactionType.TICKET_PURCHASE.getValue()),
                anyString(), eq(userId), eq(amount), anyString(), eq(data)))
                .thenReturn(mockTx);
        when(repository.update(mockTx)).thenReturn(mockTx);

        // act
        Transaction result = service.createTicketPurchaseTransaction(userId, amount, data);

        // assert
        verify(userService).deductBalance(userId, amount);
        verify(mockTx).setStatus(TransactionStatus.SUCCESS.getValue());
        verify(repository).update(mockTx);
        assertSame(mockTx, result);
    }

    @Test
    void testTicketPurchase_FailureOnDeduct_shouldSetFailed() {
        // arrange
        Transaction mockTx = mock(Transaction.class);
        when(repository.createAndSave(anyString(), anyString(), anyString(), anyDouble(), anyString(), anyMap()))
                .thenReturn(mockTx);
        when(repository.update(mockTx)).thenReturn(mockTx);
        doThrow(new RuntimeException("deduct error"))
                .when(userService).deductBalance(userId, amount);

        // act
        Transaction result = service.createTicketPurchaseTransaction(userId, amount, data);

        // assert
        verify(userService).deductBalance(userId, amount);
        verify(mockTx).setStatus(TransactionStatus.FAILED.getValue());
        verify(repository).update(mockTx);
        assertSame(mockTx, result);
    }

    @Test
    void testDeleteTransactionByUser_shouldThrowSecurityException() {
        String currentUserId = "some-user-id";
        service.initStrategy(false, currentUserId);

        UnsupportedOperationException ex = assertThrows(
                UnsupportedOperationException.class,
                () -> service.deleteTransaction("any-tx-id", false)
        );
        assertEquals(
                "User cannot delete transactions.",
                ex.getMessage()
        );
    }


    @Test
    void testDeleteTransactionByAdmin_shouldCallRepository() {
        service.initStrategy(true, userId);
        service.deleteTransaction("tx-123", true);
        verify(repository).deleteById("tx-123");
    }
}
