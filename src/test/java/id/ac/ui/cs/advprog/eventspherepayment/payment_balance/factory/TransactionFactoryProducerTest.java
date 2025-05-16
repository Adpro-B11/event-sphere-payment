package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.factory;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.PaymentMethod;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TransactionFactoryProducerTest {

    private Map<String, String> paymentData;
    private Map<String, String> ticketData;

    @BeforeEach
    void setUp() {
        paymentData = new HashMap<>();
        ticketData = new HashMap<>();
    }

    @Test
    void testGetFactoryForTopUpTransaction() {
        paymentData.put("bankName", "Bank ABC");
        paymentData.put("accountNumber", "1234567890");

        TransactionFactory factory = TransactionFactoryProducer.getFactory(
                TransactionType.TOPUP_BALANCE.name(),
                "txn-001",
                "user-123",
                50000,
                PaymentMethod.BANK_TRANSFER.name(),
                paymentData
        );

        assertTrue(factory instanceof TopUpTransactionFactory);
    }

    @Test
    void testGetFactoryForTicketPurchaseTransaction() {
        ticketData.put("ConcertVIP", "3");

        TransactionFactory factory = TransactionFactoryProducer.getFactory(
                TransactionType.TICKET_PURCHASE.name(),
                "txn-207",
                "user-207",
                100000,
                null,
                ticketData
        );

        assertInstanceOf(TicketPurchaseTransactionFactory.class, factory);
    }

    @Test
    void testGetFactoryForInvalidTransactionType() {
        assertThrows(IllegalArgumentException.class, () -> {
            TransactionFactoryProducer.getFactory(
                    "INVALID_TYPE",
                    "txn-002",
                    "user-456",
                    50000,
                    PaymentMethod.BANK_TRANSFER.name(),
                    paymentData
            );
        });
    }
}
