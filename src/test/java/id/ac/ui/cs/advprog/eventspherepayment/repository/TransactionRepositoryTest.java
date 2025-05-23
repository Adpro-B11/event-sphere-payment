package id.ac.ui.cs.advprog.eventspherepayment.repository;

import id.ac.ui.cs.advprog.eventspherepayment.enums.PaymentMethod;
import id.ac.ui.cs.advprog.eventspherepayment.enums.TransactionStatus;
import id.ac.ui.cs.advprog.eventspherepayment.enums.TransactionType;
import id.ac.ui.cs.advprog.eventspherepayment.model.TicketPurchaseTransaction;
import id.ac.ui.cs.advprog.eventspherepayment.model.TopUpTransaction;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Import(TransactionRepository.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private TestEntityManager em;

    private UUID txId1;
    private UUID txId2;
    private UUID userId1;
    private UUID userId2;

    @BeforeEach
    void setUp() {
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        txId1 = UUID.fromString("ab098978-37c8-4150-8dee-04c6acf7a490");
        txId2 = UUID.fromString("ab098978-37c8-4150-8dee-04c6acf7a491");

        // Persist a successful TicketPurchaseTransaction
        Map<String, String> ticketData = Map.of(
                "VIP", "2",
                "REGULAR", "5",
                "PREMIUM", "1"
        );
        TicketPurchaseTransaction t1 = new TicketPurchaseTransaction(
                txId1,
                userId1,
                TransactionType.TICKET_PURCHASE.getValue(),
                100000.0,
                PaymentMethod.IN_APP_BALANCE.getValue(),
                ticketData
        );
        t1.setStatus(TransactionStatus.SUCCESS.getValue());
        em.persist(t1);

        // Persist a successful TopUpTransaction
        Map<String, String> payData = Map.of("accountNumber", "6631286683123456");
        TopUpTransaction t2 = new TopUpTransaction(
                txId2,
                userId2,
                TransactionType.TOPUP_BALANCE.getValue(),
                50000.0,
                PaymentMethod.CREDIT_CARD.getValue(),
                payData
        );
        t2.setStatus(TransactionStatus.SUCCESS.getValue());
        em.persist(t2);

        em.flush();
    }

    @Test
    void testCreateAndSaveTicketPurchase_SetsPending() {
        Map<String, String> data = Map.of("VIP", "3", "REGULAR", "10");
        String newTx = "ab098978-37c8-4150-8dee-04c6acf7a492";

        Transaction tx = repository.createAndSave(
                TransactionType.TICKET_PURCHASE.getValue(),
                newTx,
                userId1.toString(),
                150000.0,
                PaymentMethod.IN_APP_BALANCE.getValue(),
                data
        );

        assertNotNull(tx);
        assertEquals(UUID.fromString(newTx), tx.getTransactionId());
        assertEquals(userId1, tx.getUserId());
        assertEquals(TransactionStatus.PENDING.getValue(), tx.getStatus());
    }

    @Test
    void testCreateAndSaveTopUp_SetsPending() {
        Map<String, String> data = Map.of("bankName", "Bank BCA", "accountNumber", "1234567890");
        String newTx = "ab098978-37c8-4150-8dee-04c6acf7a493";

        Transaction tx = repository.createAndSave(
                TransactionType.TOPUP_BALANCE.getValue(),
                newTx,
                userId2.toString(),
                75000.0,
                PaymentMethod.BANK_TRANSFER.getValue(),
                data
        );

        assertNotNull(tx);
        assertEquals(UUID.fromString(newTx), tx.getTransactionId());
        assertEquals(userId2, tx.getUserId());
        assertEquals(TransactionStatus.PENDING.getValue(), tx.getStatus());
    }

    @Test
    void testSaveAndFindById_Persisted() {
        Optional<Transaction> found = repository.findById(txId1.toString());
        assertTrue(found.isPresent());
        assertEquals(txId1, found.get().getTransactionId());
    }

    @Test
    void testFindAll_ReturnsTwo() {
        List<Transaction> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testFindByUserId_FiltersCorrectly() {
        List<Transaction> list = repository.findByFilters(
                userId2.toString(), null, null, null, null, null
        );
        assertEquals(1, list.size());
        assertEquals(txId2, list.getFirst().getTransactionId());
    }

    @Test
    void testFindByStatus_FiltersCorrectly() {
        List<Transaction> list = repository.findByFilters(
                null, TransactionStatus.SUCCESS.getValue(), null, null, null, null
        );
        assertEquals(2, list.size());
    }

    @Test
    void testFindByType_FiltersTicket() {
        List<Transaction> list = repository.findByFilters(
                null, null, TransactionType.TICKET_PURCHASE.getValue(), null, null, null
        );
        assertEquals(1, list.size());
        assertEquals(txId1, list.getFirst().getTransactionId());
    }

    @Test
    void testFindByMultipleFilters_UserAndType() {
        List<Transaction> list = repository.findByFilters(
                userId1.toString(), TransactionStatus.SUCCESS.getValue(), TransactionType.TICKET_PURCHASE.getValue(), null, null, null
        );
        assertEquals(1, list.size());
        assertEquals(txId1, list.getFirst().getTransactionId());
    }

    @Test
    void testDeleteById_RemovesEntity() {
        repository.deleteById(txId1.toString());
        assertFalse(repository.findById(txId1.toString()).isPresent());
    }

    @Test
    void testCreateInvalidType_Throws() {
        assertThrows(IllegalArgumentException.class, () -> repository.createAndSave(
                "INVALID", "dummy", userId1.toString(), 0.0, "X", Collections.emptyMap()
        ));
    }

    @Test
    void testSaveNullTransaction_Throws() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    void testSaveTransactionWithNullId_Throws() {
        assertThrows(IllegalArgumentException.class, () -> new TicketPurchaseTransaction(
                null, userId1, TransactionType.TICKET_PURCHASE.getValue(), 0.0, PaymentMethod.IN_APP_BALANCE.getValue(), Collections.emptyMap()
        ));
    }

    @Test
    void testFindByIdNotFound_ReturnsEmpty() {
        assertTrue(repository.findById("no-id").isEmpty());
    }

    @Test
    void testFindByUserIdNotFound_ReturnsEmpty() {
        assertTrue(repository.findByFilters("unknown", null, null, null, null, null).isEmpty());
    }

    @Test
    void testFindByStatusNotFound_ReturnsEmpty() {
        List<Transaction> list = repository.findByFilters(
                null, TransactionStatus.FAILED.getValue(), null, null, null, null
        );
        assertTrue(list.isEmpty());
    }

    @Test
    void testFindByTypeNotFound_ReturnsEmpty() {
        List<Transaction> list = repository.findByFilters(
                null, null, "OTHER", null, null, null
        );
        assertTrue(list.isEmpty());
    }

    @Test
    void testFindAllTicketPurchasesWhenNoneExist() {
        repository.deleteById(txId1.toString());
        List<Transaction> list = repository.findByFilters(
                null, null, TransactionType.TICKET_PURCHASE.getValue(), null, null, null
        );
        assertTrue(list.isEmpty());
    }

    @Test
    void testFindAllTopUpsWhenNoneExist() {
        repository.deleteById(txId2.toString());
        List<Transaction> list = repository.findByFilters(
                null, null, TransactionType.TOPUP_BALANCE.getValue(), null, null, null
        );
        assertTrue(list.isEmpty());
    }

    @Test
    void testCreateAndSaveWithInvalidData_Throws() {
        assertThrows(IllegalArgumentException.class, () -> repository.createAndSave(
                TransactionType.TOPUP_BALANCE.getValue(), UUID.randomUUID().toString(), userId2.toString(), 100, PaymentMethod.BANK_TRANSFER.getValue(), Map.of("accountNumber","123")
        ));
    }

    @Test
    void testTicketPurchaseWithMultipleTicketTypes_Succeeds() {
        Map<String, String> multi = Map.of("VIP", "2", "REGULAR", "10", "ECONOMY", "5", "BACKSTAGE", "1");
        Transaction tx = repository.createAndSave(
                TransactionType.TICKET_PURCHASE.getValue(), UUID.randomUUID().toString(), userId1.toString(), 200000, PaymentMethod.IN_APP_BALANCE.getValue(), multi
        );
        assertInstanceOf(TicketPurchaseTransaction.class, tx);
        TicketPurchaseTransaction tpt = (TicketPurchaseTransaction) tx;
        assertEquals("2", tpt.getTicketData().get("VIP"));
        assertEquals("1", tpt.getTicketData().get("BACKSTAGE"));
    }

    @Test
    void testTicketPurchaseWithEmptyTicketData_Throws() {
        assertThrows(IllegalArgumentException.class, () -> repository.createAndSave(
                TransactionType.TICKET_PURCHASE.getValue(), UUID.randomUUID().toString(), userId1.toString(), 0, PaymentMethod.IN_APP_BALANCE.getValue(), Collections.emptyMap()
        ));
    }

    @Test
    void testTicketPurchaseWithNumericQuantities_Persists() {
        Map<String, String> small = Map.of("VIP", "2", "REGULAR", "3");
        String newId = UUID.randomUUID().toString();
        Transaction tx = repository.createAndSave(
                TransactionType.TICKET_PURCHASE.getValue(), newId, userId1.toString(), 150000, PaymentMethod.IN_APP_BALANCE.getValue(), small
        );
        Optional<Transaction> found = repository.findById(newId);
        assertTrue(found.isPresent());
        assertEquals("2", ((TicketPurchaseTransaction) found.get()).getTicketData().get("VIP"));
    }
}
