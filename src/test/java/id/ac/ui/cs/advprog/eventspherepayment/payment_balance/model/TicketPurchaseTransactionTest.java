package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.TransactionStatus;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.TransactionType;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.factory.TransactionFactory;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.factory.TransactionFactoryProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TicketPurchaseTransactionTest {

    private Map<String, String> ticketData;

    @BeforeEach
    void setUp() {
        ticketData = new HashMap<>();
    }

    @Test
    void testEmptyTicketDataThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            TransactionFactory factory = TransactionFactoryProducer.getFactory(
                    TransactionType.TICKET_PURCHASE.name(),
                    "txn-201",
                    "user-201",
                    100000,
                    null,
                    ticketData
            );

            factory.createTransaction();
        });
    }

    @Test
    void testTicketKeyIsNullThrowsException() {
        ticketData.put(null, "1");

        assertThrows(IllegalArgumentException.class, () -> {
            TransactionFactory factory = TransactionFactoryProducer.getFactory(
                    TransactionType.TICKET_PURCHASE.name(),
                    "txn-202",
                    "user-202",
                    100000,
                    null,
                    ticketData
            );

            factory.createTransaction();
        });
    }

    @Test
    void testTicketKeyIsEmptyStringThrowsException() {
        ticketData.put("", "1");

        assertThrows(IllegalArgumentException.class, () -> {
            TransactionFactory factory = TransactionFactoryProducer.getFactory(
                    TransactionType.TICKET_PURCHASE.name(),
                    "txn-203",
                    "user-203",
                    100000,
                    null,
                    ticketData
            );

            factory.createTransaction();
        });
    }

    @Test
    void testTicketKeyIsWhitespaceOnlyThrowsException() {
        ticketData.put("   ", "1");

        assertThrows(IllegalArgumentException.class, () -> {
            TransactionFactory factory = TransactionFactoryProducer.getFactory(
                    TransactionType.TICKET_PURCHASE.name(),
                    "txn-204",
                    "user-204",
                    100000,
                    null,
                    ticketData
            );

            factory.createTransaction();
        });
    }

    @Test
    void testNegativeTicketAmountThrowsException() {
        ticketData.put("ConcertVIP", "-3");

        assertThrows(IllegalArgumentException.class, () -> {
            TransactionFactory factory = TransactionFactoryProducer.getFactory(
                    TransactionType.TICKET_PURCHASE.name(),
                    "txn-205",
                    "user-205",
                    100000,
                    null,
                    ticketData
            );

            factory.createTransaction();
        });
    }

    @Test
    void testZeroTicketAmountThrowsException() {
        ticketData.put("ConcertVIP", "0");

        assertThrows(IllegalArgumentException.class, () -> {
            TransactionFactory factory = TransactionFactoryProducer.getFactory(
                    TransactionType.TICKET_PURCHASE.name(),
                    "txn-206",
                    "user-206",
                    100000,
                    null,
                    ticketData
            );

            factory.createTransaction();
        });
    }

    @Test
    void testTicketPurchaseSuccess() {
        ticketData.put("ConcertVIP", "3");
        ticketData.put("ConcertVVIP", "2");

        TransactionFactory factory = TransactionFactoryProducer.getFactory(
                TransactionType.TICKET_PURCHASE.name(),
                "txn-207",
                "user-207",
                100000,
                null,
                ticketData
        );

        Transaction transaction = factory.createTransaction();
        ((TicketPurchaseTransaction) transaction).setValidateStatus();

        assertEquals("txn-207", transaction.getTransactionId());
        assertEquals("user-207", transaction.getUserId());
        assertEquals(TransactionType.TICKET_PURCHASE.name(), transaction.getType());
        assertEquals(TransactionStatus.SUCCESS.name(), transaction.getStatus());
        assertEquals(100000, transaction.getAmount());

        // Verify ticket data is preserved
        Map<String, String> purchasedTickets = ((TicketPurchaseTransaction) transaction).getTicketData();
        assertEquals("3", purchasedTickets.get("ConcertVIP"));
        assertEquals("2", purchasedTickets.get("ConcertVVIP"));
    }

    @Test
    void testTicketPurchaseWithMultipleTicketTypes() {
        ticketData.put("Regular", "5");
        ticketData.put("VIP", "2");
        ticketData.put("VVIP", "1");

        TransactionFactory factory = TransactionFactoryProducer.getFactory(
                TransactionType.TICKET_PURCHASE.name(),
                "txn-208",
                "user-208",
                250000,
                null,
                ticketData
        );

        Transaction transaction = factory.createTransaction();
        ((TicketPurchaseTransaction) transaction).setValidateStatus();

        assertEquals("txn-208", transaction.getTransactionId());
        assertEquals(TransactionStatus.SUCCESS.name(), transaction.getStatus());

        // Verify ticket data
        Map<String, String> purchasedTickets = ((TicketPurchaseTransaction) transaction).getTicketData();
        assertEquals("5", purchasedTickets.get("Regular"));
        assertEquals("2", purchasedTickets.get("VIP"));
        assertEquals("1", purchasedTickets.get("VVIP"));
    }
}