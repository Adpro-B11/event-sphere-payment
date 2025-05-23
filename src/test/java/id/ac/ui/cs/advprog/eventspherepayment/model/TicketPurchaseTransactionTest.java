package id.ac.ui.cs.advprog.eventspherepayment.model;

import id.ac.ui.cs.advprog.eventspherepayment.enums.PaymentMethod;
import id.ac.ui.cs.advprog.eventspherepayment.enums.TransactionStatus;
import id.ac.ui.cs.advprog.eventspherepayment.enums.TransactionType;
import id.ac.ui.cs.advprog.eventspherepayment.factory.TransactionFactoryProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TicketPurchaseTransactionTest {

    private Map<String, String> ticketData;
    private UUID userId;

    @BeforeEach
    void setUp() {
        ticketData = new HashMap<>();
        userId = UUID.randomUUID();
    }

    @Test
    void testEmptyTicketDataThrowsException() {
        UUID txId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TICKET_PURCHASE.getValue(),
                        txId.toString(),
                        userId.toString(),
                        100_000,
                        PaymentMethod.IN_APP_BALANCE.getValue(),
                        ticketData
                ).createTransaction()
        );
    }

    @Test
    void testTicketKeyIsNullThrowsException() {
        ticketData.put(null, "1");
        UUID txId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TICKET_PURCHASE.getValue(),
                        txId.toString(),
                        userId.toString(),
                        100_000,
                        PaymentMethod.IN_APP_BALANCE.getValue(),
                        ticketData
                ).createTransaction()
        );
    }

    @Test
    void testTicketKeyIsEmptyStringThrowsException() {
        ticketData.put("", "1");
        UUID txId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TICKET_PURCHASE.getValue(),
                        txId.toString(),
                        userId.toString(),
                        100_000,
                        PaymentMethod.IN_APP_BALANCE.getValue(),
                        ticketData
                ).createTransaction()
        );
    }

    @Test
    void testTicketKeyIsWhitespaceOnlyThrowsException() {
        ticketData.put("   ", "1");
        UUID txId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TICKET_PURCHASE.getValue(),
                        txId.toString(),
                        userId.toString(),
                        100_000,
                        PaymentMethod.IN_APP_BALANCE.getValue(),
                        ticketData
                ).createTransaction()
        );
    }

    @Test
    void testNegativeTicketAmountThrowsException() {
        ticketData.put("ConcertVIP", "-3");
        UUID txId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TICKET_PURCHASE.getValue(),
                        txId.toString(),
                        userId.toString(),
                        100_000,
                        PaymentMethod.IN_APP_BALANCE.getValue(),
                        ticketData
                ).createTransaction()
        );
    }

    @Test
    void testZeroTicketAmountThrowsException() {
        ticketData.put("ConcertVIP", "0");
        UUID txId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TICKET_PURCHASE.getValue(),
                        txId.toString(),
                        userId.toString(),
                        100_000,
                        PaymentMethod.IN_APP_BALANCE.getValue(),
                        ticketData
                ).createTransaction()
        );
    }

    @Test
    void testTicketPurchasePending() {
        ticketData.put("ConcertVIP", "3");
        ticketData.put("ConcertVVIP", "2");
        UUID txId = UUID.randomUUID();

        var transaction = TransactionFactoryProducer.getFactory(
                TransactionType.TICKET_PURCHASE.getValue(),
                txId.toString(),
                userId.toString(),
                100_000,
                PaymentMethod.IN_APP_BALANCE.getValue(),
                ticketData
        ).createTransaction();

        assertEquals(txId, transaction.getTransactionId());
        assertEquals(userId, transaction.getUserId());
        assertEquals(TransactionType.TICKET_PURCHASE.getValue(), transaction.getType());
        assertEquals(PaymentMethod.IN_APP_BALANCE.getValue(), ((TicketPurchaseTransaction) transaction).getMethod());
        assertEquals(TransactionStatus.PENDING.getValue(), transaction.getStatus());
        assertEquals(100_000, transaction.getAmount());

        Map<String, String> purchasedTickets = ((TicketPurchaseTransaction) transaction).getTicketData();
        assertEquals("3", purchasedTickets.get("ConcertVIP"));
        assertEquals("2", purchasedTickets.get("ConcertVVIP"));
    }

    @Test
    void testTicketPurchaseWithMultipleTypesPending() {
        ticketData.put("Regular", "5");
        ticketData.put("VIP", "2");
        ticketData.put("VVIP", "1");
        UUID txId = UUID.randomUUID();

        var transaction = TransactionFactoryProducer.getFactory(
                TransactionType.TICKET_PURCHASE.getValue(),
                txId.toString(),
                userId.toString(),
                250_000,
                PaymentMethod.IN_APP_BALANCE.getValue(),
                ticketData
        ).createTransaction();

        assertEquals(txId, transaction.getTransactionId());
        assertEquals(TransactionStatus.PENDING.getValue(), transaction.getStatus());

        Map<String, String> purchasedTickets = ((TicketPurchaseTransaction) transaction).getTicketData();
        assertEquals("5", purchasedTickets.get("Regular"));
        assertEquals("2", purchasedTickets.get("VIP"));
        assertEquals("1", purchasedTickets.get("VVIP"));
    }
}
