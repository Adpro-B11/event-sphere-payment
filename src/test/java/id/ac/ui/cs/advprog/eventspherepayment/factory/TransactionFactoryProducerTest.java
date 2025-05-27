package id.ac.ui.cs.advprog.eventspherepayment.factory;

import id.ac.ui.cs.advprog.eventspherepayment.enums.PaymentMethod;
import id.ac.ui.cs.advprog.eventspherepayment.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionFactoryProducerTest {

    private Map<String, String> paymentData;
    private Map<String, String> ticketData;
    private String transactionId;
    private String userId;

    @BeforeEach
    void setUp() {
        paymentData = new HashMap<>();
        ticketData = new HashMap<>();
        transactionId = UUID.randomUUID().toString();
        userId = UUID.randomUUID().toString();
    }

    @Test
    void testGetFactoryForTopUpTransaction() {
        paymentData.put("bankName", "Bank ABC");
        paymentData.put("accountNumber", "1234567890");

        TransactionFactory factory = TransactionFactoryProducer.getFactory(
                TransactionType.TOPUP_BALANCE.getValue(),
                transactionId,
                userId,
                null,
                50000,
                PaymentMethod.BANK_TRANSFER.getValue(),
                paymentData
        );

        assertInstanceOf(TopUpTransactionFactory.class, factory);
    }

    @Test
    void testGetFactoryForTicketPurchaseTransaction() {
        ticketData.put("ConcertVIP", "3");

        TransactionFactory factory = TransactionFactoryProducer.getFactory(
                TransactionType.TICKET_PURCHASE.getValue(),
                transactionId,
                userId,
                UUID.randomUUID().toString(),
                100000,
                PaymentMethod.IN_APP_BALANCE.getValue(),
                ticketData
        );

        assertInstanceOf(TicketPurchaseTransactionFactory.class, factory);
    }

    @Test
    void testGetFactoryForInvalidTransactionType() {
        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        "INVALID_TYPE",
                        transactionId,
                        userId,
                        null,
                        50000,
                        PaymentMethod.BANK_TRANSFER.getValue(),
                        paymentData
                )
        );
    }
}
