package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.service;

import id.ac.ui.cs.advprog.eventspherepayment.auth.service.UserService;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.TransactionStatus;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.TransactionType;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.repository.TransactionRepository;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.factory.TransactionFactoryProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userBalanceService;

    @Mock
    private TransactionFactoryProducer factoryProducer;    // only needed by UserAccessStrategy

    private UserAccessStrategy userStrategy;
    private TransactionServiceImpl service;

    private final String transactionId = UUID.randomUUID().toString();
    private final String userId        = UUID.randomUUID().toString();
    private final double amount        = 100.0;
    private final String method        = "BANK_TRANSFER";
    private final Map<String, String> data = Map.of("accountNumber", "123456");

    @BeforeEach
    void setUp() {
        // create the two objects under test
        userStrategy = new UserAccessStrategy(transactionRepository, factoryProducer);
        service      = new TransactionServiceImpl(userBalanceService);
    }

    @Test
    void testTopUpBalance_Success_shouldCallAddBalance() {
        // arrange: repository returns a transaction whose status is SUCCESS
        Transaction mockTrx = mock(Transaction.class);
        when(transactionRepository.createAndSave(
                TransactionType.TOPUP_BALANCE.name(),
                transactionId, userId, amount, method, data))
                .thenReturn(mockTrx);

        when(mockTrx.getStatus())
                .thenReturn(TransactionStatus.SUCCESS.name());

        // act
        service.setStrategy(userStrategy);
        service.createTransaction(
                TransactionType.TOPUP_BALANCE.name(),
                transactionId, userId, amount, method, data);

        // assert: repo was used and balance was added
        verify(transactionRepository).createAndSave(
                TransactionType.TOPUP_BALANCE.name(),
                transactionId, userId, amount, method, data);

        verify(userBalanceService).addBalance(userId, amount);
    }

    @Test
    void testTicketPurchase_Failed_shouldDeductThenRefund() {
        // arrange: deductBalance returns true
        when(userBalanceService.deductBalance(userId, amount)).thenReturn(true);

        // repository returns a transaction whose status is FAILED
        Transaction mockTrx = mock(Transaction.class);
        when(transactionRepository.createAndSave(
                TransactionType.TICKET_PURCHASE.name(),
                transactionId, userId, amount, method, data))
                .thenReturn(mockTrx);

        when(mockTrx.getStatus())
                .thenReturn(TransactionStatus.FAILED.name());

        // act
        service.setStrategy(userStrategy);
        service.createTransaction(
                TransactionType.TICKET_PURCHASE.name(),
                transactionId, userId, amount, method, data);

        // assert: balance was first deducted, then refunded on failure
        verify(userBalanceService).deductBalance(userId, amount);
        verify(transactionRepository).createAndSave(
                TransactionType.TICKET_PURCHASE.name(),
                transactionId, userId, amount, method, data);
        verify(userBalanceService).addBalance(userId, amount);
    }

    @Test
    void testDeleteTransactionByUserStrategy_ThrowsUnsupported() {
        service.setStrategy(userStrategy);

        assertThrows(UnsupportedOperationException.class, () ->
                service.deleteTransaction("any-id")
        );
    }

    @Test
    void testViewAllTransactionsByUserStrategy_ThrowsUnsupported() {
        service.setStrategy(userStrategy);

        assertThrows(UnsupportedOperationException.class, () ->
                service.viewAllTransactions()
        );
    }

    @Test
    void testFilterTransactionsByUserStrategy_ThrowsUnsupported() {
        service.setStrategy(userStrategy);

        assertThrows(UnsupportedOperationException.class, () ->
                service.filterTransactions(null,"SUCCESS",null)
        );
    }

}
