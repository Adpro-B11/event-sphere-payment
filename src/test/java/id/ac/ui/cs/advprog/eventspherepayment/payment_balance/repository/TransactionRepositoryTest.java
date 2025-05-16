package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.repository;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.*;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.factory.TransactionFactoryProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionRepositoryTest {

    private TransactionRepository repository;
    private TransactionFactoryProducer factoryProducer;
    private List<Transaction> transactionList;

    @BeforeEach
    void setUp() {
        factoryProducer = new TransactionFactoryProducer();
        repository = new TransactionRepository(factoryProducer);

        // Bank transfer payment data for topup transaction
        Map<String, String> paymentData1 = new HashMap<>();
        paymentData1.put("bankName", "Bank BCA");
        paymentData1.put("accountNumber", "6631286837");

        // Credit card payment data
        Map<String, String> paymentData2 = new HashMap<>();
        paymentData2.put("accountNumber", "6631286683123456");

        // Ticket purchase data with ticket types and quantities
        Map<String, String> ticketData = new HashMap<>();
        ticketData.put("VIP", "2");
        ticketData.put("REGULAR", "5");
        ticketData.put("PREMIUM", "1");

        TicketPurchaseTransaction trx1 = new TicketPurchaseTransaction("trx-001", "user-01", "TICKET_PURCHASE", 100000, ticketData);
        TopUpTransaction trx2 = new TopUpTransaction("trx-002", "user-02", "TOPUP_BALANCE", "CREDIT_CARD", 50000, paymentData2);
        transactionList = List.of(trx1, trx2);
    }

    @Test
    void testCreateAndSaveTicketPurchase() {
        // Create ticket data with ticket types and quantities
        Map<String, String> ticketData = new HashMap<>();
        ticketData.put("VIP", "3");
        ticketData.put("REGULAR", "10");

        Transaction transaction = repository.createAndSave(
                "TICKET_PURCHASE",
                "trx-003",
                "user-03",
                150000,
                null, // Payment method not needed for ticket purchase
                ticketData
        );

        assertNotNull(transaction);
        assertEquals("trx-003", transaction.getTransactionId());
        assertTrue(transaction instanceof TicketPurchaseTransaction);

        // Verify ticket data
        TicketPurchaseTransaction ticketPurchase = (TicketPurchaseTransaction) transaction;
        Map<String, String> purchaseData = ticketPurchase.getTicketData();
        assertEquals("3", purchaseData.get("VIP"));
        assertEquals("10", purchaseData.get("REGULAR"));
    }

    @Test
    void testCreateAndSaveTopUp() {
        Map<String, String> paymentData = new HashMap<>();
        paymentData.put("bankName", "Bank BCA");
        paymentData.put("accountNumber", "1234567890");

        Transaction transaction = repository.createAndSave(
                "TOPUP_BALANCE",
                "trx-004",
                "user-04",
                75000,
                "BANK_TRANSFER",
                paymentData
        );

        assertNotNull(transaction);
        assertEquals("trx-004", transaction.getTransactionId());
        assertTrue(transaction instanceof TopUpTransaction);

        // Verify payment data
        TopUpTransaction topUp = (TopUpTransaction) transaction;
        assertEquals("BANK_TRANSFER", topUp.getMethod());
        Map<String, String> actualPaymentData = topUp.getPaymentData();
        assertEquals("Bank BCA", actualPaymentData.get("bankName"));
        assertEquals("1234567890", actualPaymentData.get("accountNumber"));
    }

    @Test
    void testSaveAndFindById() {
        repository.save(transactionList.get(0));
        Transaction found = repository.findById("trx-001").orElse(null);

        assertNotNull(found);
        assertEquals("trx-001", found.getTransactionId());
        assertEquals("user-01", found.getUserId());

        // Verify it's a ticket purchase with correct data
        assertTrue(found instanceof TicketPurchaseTransaction);
        TicketPurchaseTransaction ticketPurchase = (TicketPurchaseTransaction) found;
        Map<String, String> ticketData = ticketPurchase.getTicketData();
        assertEquals("2", ticketData.get("VIP"));
        assertEquals("5", ticketData.get("REGULAR"));
        assertEquals("1", ticketData.get("PREMIUM"));
    }

    @Test
    void testFindByUserId() {
        transactionList.forEach(repository::save);
        List<Transaction> result = repository.findByFilters("user-02", null, null);

        assertEquals(1, result.size());
        assertEquals("trx-002", result.get(0).getTransactionId());
        assertTrue(result.get(0) instanceof TopUpTransaction);
    }

    @Test
    void testFindAll() {
        transactionList.forEach(repository::save);
        List<Transaction> all = repository.findAll();

        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(trx -> trx.getTransactionId().equals("trx-001")));
        assertTrue(all.stream().anyMatch(trx -> trx.getTransactionId().equals("trx-002")));
    }

    @Test
    void testFindByStatus() {
        transactionList.forEach(repository::save);
        List<Transaction> successList = repository.findByFilters(null, "SUCCESS", null);

        assertEquals(2, successList.size());
        assertTrue(successList.stream().allMatch(trx -> trx.getStatus().equals("SUCCESS")));
    }

    @Test
    void testFindByType() {
        transactionList.forEach(repository::save);
        List<Transaction> ticketPurchaseList = repository.findByFilters(null, null, "TICKET_PURCHASE");

        assertEquals(1, ticketPurchaseList.size());
        assertEquals("trx-001", ticketPurchaseList.get(0).getTransactionId());
        assertTrue(ticketPurchaseList.get(0) instanceof TicketPurchaseTransaction);
    }

    @Test
    void testFindAllTicketPurchases() {
        transactionList.forEach(repository::save);
        List<Transaction> ticketPurchases = repository.findByFilters(null, null, "TICKET_PURCHASE");

        assertEquals(1, ticketPurchases.size());
        assertEquals("trx-001", ticketPurchases.get(0).getTransactionId());
        assertTrue(ticketPurchases.get(0) instanceof TicketPurchaseTransaction);

        // Verify ticket data
        TicketPurchaseTransaction ticketPurchase = (TicketPurchaseTransaction) ticketPurchases.get(0);
        Map<String, String> ticketData = ticketPurchase.getTicketData();
        assertEquals("2", ticketData.get("VIP"));
        assertEquals("5", ticketData.get("REGULAR"));
        assertEquals("1", ticketData.get("PREMIUM"));
    }

    @Test
    void testFindAllTopUps() {
        transactionList.forEach(repository::save);
        List<Transaction> topUps = repository.findByFilters(null, null, "TOPUP_BALANCE");

        assertEquals(1, topUps.size());
        assertEquals("trx-002", topUps.get(0).getTransactionId());
        assertTrue(topUps.get(0) instanceof TopUpTransaction);

        TopUpTransaction topUp = (TopUpTransaction) topUps.get(0);
        assertEquals("CREDIT_CARD", topUp.getMethod());
    }

    @Test
    void testFindByMultipleFilters() {
        transactionList.forEach(repository::save);
        List<Transaction> filteredList = repository.findByFilters("user-01", "SUCCESS", "TICKET_PURCHASE");

        assertEquals(1, filteredList.size());
        assertEquals("trx-001", filteredList.get(0).getTransactionId());
        assertTrue(filteredList.get(0) instanceof TicketPurchaseTransaction);
    }

    @Test
    void testDeleteById() {
        transactionList.forEach(repository::save);
        repository.deleteById("trx-001");

        Transaction found = repository.findById("trx-001").orElse(null);
        assertNull(found);

        List<Transaction> remaining = repository.findAll();
        assertEquals(1, remaining.size());
        assertEquals("trx-002", remaining.get(0).getTransactionId());
    }

    @Test
    void testCreateAndSaveWithInvalidType() {
        assertThrows(IllegalArgumentException.class, () -> {
            repository.createAndSave("INVALID_TYPE", "trx-001", "user-01", 100000,
                    "BANK_TRANSFER", Map.of("bankName", "Bank BCA"));
        });
    }

    @Test
    void testSaveNullTransaction() {
        assertThrows(NullPointerException.class, () -> {
            repository.save(null);
        });
    }

    @Test
    void testSaveTransactionWithNullId() {
        Map<String, String> ticketData = new HashMap<>();
        ticketData.put("VIP", "1");

        TicketPurchaseTransaction trx = new TicketPurchaseTransaction(
                null, "user-01", "TICKET_PURCHASE", 100000, ticketData
        );
        assertThrows(NullPointerException.class, () -> {
            repository.save(trx);
        });
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Transaction> result = repository.findById("non-existent-id");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByUserIdNotFound() {
        List<Transaction> result = repository.findByFilters("non-existent-user", null, null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByStatusNotFound() {
        Map<String, String> ticketData = new HashMap<>();
        ticketData.put("REGULAR", "3");

        repository.save(new TicketPurchaseTransaction("trx-001", "user-01", "TICKET_PURCHASE",
                50000, ticketData));

        List<Transaction> result = repository.findByFilters(null, "FAILED", null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByTypeNotFound() {
        Map<String, String> paymentData = new HashMap<>();
        paymentData.put("accountNumber", "123");
        paymentData.put("bankName", "BCA");

        repository.save(new TopUpTransaction("trx-001", "user-01", "TOPUP_BALANCE",
                "BANK_TRANSFER", 50000, paymentData));

        List<Transaction> result = repository.findByFilters(null, null, "TICKET_PURCHASE");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAllTicketPurchasesWhenNoneExist() {
        Map<String, String> paymentData = new HashMap<>();
        paymentData.put("accountNumber", "123");
        paymentData.put("bankName", "BCA");

        repository.save(new TopUpTransaction("trx-001", "user-01", "TOPUP_BALANCE",
                "BANK_TRANSFER", 50000, paymentData));

        List<Transaction> result = repository.findByFilters(null, null, "TICKET_PURCHASE");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAllTopUpsWhenNoneExist() {
        Map<String, String> ticketData = new HashMap<>();
        ticketData.put("VIP", "1");
        ticketData.put("REGULAR", "2");

        repository.save(new TicketPurchaseTransaction("trx-001", "user-01", "TICKET_PURCHASE",
                100000, ticketData));

        List<Transaction> result = repository.findByFilters(null, null, "TOPUP_BALANCE");
        assertTrue(result.isEmpty());
    }

    @Test
    void testDeleteNonExistentId() {
        // Should not throw exception
        assertDoesNotThrow(() -> {
            repository.deleteById("non-existent-id");
        });
    }

    @Test
    void testCreateAndSaveWithInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> {
            // Missing required bankName for BANK_TRANSFER
            repository.createAndSave("TOPUP_BALANCE", "trx-001", "user-01", 100000,
                    "BANK_TRANSFER", Map.of("accountNumber", "123"));
        });
    }

    @Test
    void testSaveInvalidTransactionStatus() {
        Map<String, String> ticketData = new HashMap<>();
        ticketData.put("VIP", "1");

        TicketPurchaseTransaction trx = new TicketPurchaseTransaction(
                "trx-001", "user-01", "TICKET_PURCHASE", 100000, ticketData
        );
        assertThrows(IllegalArgumentException.class, () -> {
            trx.setStatus("INVALID_STATUS");
            repository.save(trx);
        });
    }

    @Test
    void testTicketPurchaseWithMultipleTicketTypes() {
        Map<String, String> ticketData = new HashMap<>();
        ticketData.put("VIP", "2");
        ticketData.put("REGULAR", "10");
        ticketData.put("ECONOMY", "5");
        ticketData.put("BACKSTAGE", "1");

        Transaction transaction = repository.createAndSave(
                "TICKET_PURCHASE",
                "trx-005",
                "user-05",
                200000,
                null,
                ticketData
        );

        assertNotNull(transaction);
        assertTrue(transaction instanceof TicketPurchaseTransaction);

        TicketPurchaseTransaction ticketPurchase = (TicketPurchaseTransaction) transaction;
        Map<String, String> purchaseData = ticketPurchase.getTicketData();

        assertEquals("2", purchaseData.get("VIP"));
        assertEquals("10", purchaseData.get("REGULAR"));
        assertEquals("5", purchaseData.get("ECONOMY"));
        assertEquals("1", purchaseData.get("BACKSTAGE"));
    }

    @Test
    void testTicketPurchaseWithEmptyTicketData() {
        Map<String, String> ticketData = new HashMap<>();

        assertThrows(IllegalArgumentException.class, () -> {
            repository.createAndSave(
                    "TICKET_PURCHASE",
                    "trx-006",
                    "user-06",
                    0,
                    null,
                    ticketData
            );});

    }

    @Test
    void testTicketPurchaseWithNumericQuantities() {
        Map<String, String> ticketData = new HashMap<>();
        ticketData.put("VIP", "2");
        ticketData.put("REGULAR", "3");

        TicketPurchaseTransaction trx = new TicketPurchaseTransaction(
                "trx-007",
                "user-07",
                "TICKET_PURCHASE",
                150000,
                ticketData
        );

        repository.save(trx);

        Transaction retrieved = repository.findById("trx-007").orElse(null);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof TicketPurchaseTransaction);

        TicketPurchaseTransaction ticketPurchase = (TicketPurchaseTransaction) retrieved;
        Map<String, String> purchaseData = ticketPurchase.getTicketData();

        assertEquals("2", purchaseData.get("VIP"));
        assertEquals("3", purchaseData.get("REGULAR"));
    }
}