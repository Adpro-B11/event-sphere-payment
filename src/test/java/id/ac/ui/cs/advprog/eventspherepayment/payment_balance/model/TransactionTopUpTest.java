package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.PaymentMethod;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.TransactionStatus;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.TransactionType;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.factory.TransactionFactory;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.factory.TransactionFactoryProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionTopUpTest {

    private Map<String, String> paymentData;

    @BeforeEach
    void setUp() {
        paymentData = new HashMap<>();
    }

    @Test
    void testCreateTopUpTransactionSuccess() {
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

        Transaction transaction = factory.createTransaction();
        ((TopUpTransaction) transaction).setValidateStatus();

        assertEquals("txn-001", transaction.getTransactionId());
        assertEquals("user-123", transaction.getUserId());
        assertEquals(TransactionType.TOPUP_BALANCE.name(), transaction.getType());
        assertEquals(PaymentMethod.BANK_TRANSFER.name(), ((TopUpTransaction) transaction).getMethod());
        assertEquals(TransactionStatus.SUCCESS.name(), transaction.getStatus());
    }

    @Test
    void testInvalidAccountNumberTooShort() {
        paymentData.put("bankName", "Bank XYZ");
        paymentData.put("accountNumber", "12345678");

        TransactionFactory factory = TransactionFactoryProducer.getFactory(
                TransactionType.TOPUP_BALANCE.name(),
                "txn-002",
                "user-456",
                50000,
                PaymentMethod.BANK_TRANSFER.name(),
                paymentData
        );

        Transaction transaction = factory.createTransaction();
        ((TopUpTransaction) transaction).setValidateStatus();

        assertEquals(TransactionStatus.FAILED.name(), transaction.getStatus());
    }

    @Test
    void testInvalidAccountNumberWithLetters() {
        paymentData.put("bankName", "Bank DEF");
        paymentData.put("accountNumber", "1234abc890");

        TransactionFactory factory = TransactionFactoryProducer.getFactory(
                TransactionType.TOPUP_BALANCE.name(),
                "txn-003",
                "user-789",
                50000,
                PaymentMethod.BANK_TRANSFER.name(),
                paymentData
        );

        Transaction transaction = factory.createTransaction();
        ((TopUpTransaction) transaction).setValidateStatus();

        assertEquals(TransactionStatus.FAILED.name(), transaction.getStatus());
    }

    @Test
    void testInvalidAccountNumberWithSpecialCharacters() {
        paymentData.put("bankName", "Bank HIJ");
        paymentData.put("accountNumber", "12345@7890");

        TransactionFactory factory = TransactionFactoryProducer.getFactory(
                TransactionType.TOPUP_BALANCE.name(),
                "txn-004",
                "user-890",
                50000,
                PaymentMethod.BANK_TRANSFER.name(),
                paymentData
        );

        Transaction transaction = factory.createTransaction();
        ((TopUpTransaction) transaction).setValidateStatus();

        assertEquals(TransactionStatus.FAILED.name(), transaction.getStatus());
    }

    @Test
    void testInvalidAccountNumberTooLong() {
        paymentData.put("bankName", "Bank KLM");
        paymentData.put("accountNumber", "1234567890123");

        TransactionFactory factory = TransactionFactoryProducer.getFactory(
                TransactionType.TOPUP_BALANCE.name(),
                "txn-005",
                "user-999",
                50000,
                PaymentMethod.BANK_TRANSFER.name(),
                paymentData
        );

        Transaction transaction = factory.createTransaction();
        ((TopUpTransaction) transaction).setValidateStatus();

        assertEquals(TransactionStatus.FAILED.name(), transaction.getStatus());
    }

    @Test
    void testNullBankNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentData.put("bankName", null);
            paymentData.put("accountNumber", "1234567890");

            TransactionFactory factory = TransactionFactoryProducer.getFactory(
                    TransactionType.TOPUP_BALANCE.name(),
                    "txn-006",
                    "user-234",
                    50000,
                    PaymentMethod.BANK_TRANSFER.name(),
                    paymentData
            );

            factory.createTransaction();
        });
    }

    @Test
    void testEmptyBankNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentData.put("bankName", "");
            paymentData.put("accountNumber", "1234567890");

            TransactionFactory factory = TransactionFactoryProducer.getFactory(
                    TransactionType.TOPUP_BALANCE.name(),
                    "txn-007",
                    "user-345",
                    50000,
                    PaymentMethod.BANK_TRANSFER.name(),
                    paymentData
            );

            factory.createTransaction();
        });
    }

    @Test
    void testWhitespaceBankNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentData.put("bankName", "   ");
            paymentData.put("accountNumber", "1234567890");

            TransactionFactory factory = TransactionFactoryProducer.getFactory(
                    TransactionType.TOPUP_BALANCE.name(),
                    "txn-008",
                    "user-456",
                    50000,
                    PaymentMethod.BANK_TRANSFER.name(),
                    paymentData
            );

            factory.createTransaction();
        });
    }

    @Test
    void testNullAccountNumberThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentData.put("bankName", "Bank ABC");
            paymentData.put("accountNumber", null);

            TransactionFactory factory = TransactionFactoryProducer.getFactory(
                    TransactionType.TOPUP_BALANCE.name(),
                    "txn-009",
                    "user-567",
                    50000,
                    PaymentMethod.BANK_TRANSFER.name(),
                    paymentData
            );

            factory.createTransaction();
        });
    }

    @Test
    void testEmptyAccountNumberThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentData.put("bankName", "Bank DEF");
            paymentData.put("accountNumber", "");

            TransactionFactory factory = TransactionFactoryProducer.getFactory(
                    TransactionType.TOPUP_BALANCE.name(),
                    "txn-010",
                    "user-678",
                    50000,
                    PaymentMethod.BANK_TRANSFER.name(),
                    paymentData
            );

            factory.createTransaction();
        });
    }

    @Test
    void testWhitespaceAccountNumberThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentData.put("bankName", "Bank XYZ");
            paymentData.put("accountNumber", "          ");

            TransactionFactory factory = TransactionFactoryProducer.getFactory(
                    TransactionType.TOPUP_BALANCE.name(),
                    "txn-011",
                    "user-789",
                    50000,
                    PaymentMethod.BANK_TRANSFER.name(),
                    paymentData
            );

            factory.createTransaction();
        });
    }

    @Test
    void testValidCreditCardTransactionSuccess() {
        paymentData.put("accountNumber", "1234567812345678");

        TransactionFactory factory = TransactionFactoryProducer.getFactory(
                TransactionType.TOPUP_BALANCE.name(),
                "txn-100",
                "user-100",
                50000,
                PaymentMethod.CREDIT_CARD.name(),
                paymentData
        );

        Transaction transaction = factory.createTransaction();
        ((TopUpTransaction) transaction).setValidateStatus();

        assertEquals(TransactionStatus.SUCCESS.name(), transaction.getStatus());
    }

    @Test
    void testCardNumberTooShortIsRejected() {
        paymentData.put("accountNumber", "123456789012345");

        TransactionFactory factory = TransactionFactoryProducer.getFactory(
                TransactionType.TOPUP_BALANCE.name(),
                "txn-101",
                "user-101",
                50000,
                PaymentMethod.CREDIT_CARD.name(),
                paymentData
        );

        Transaction transaction = factory.createTransaction();
        ((TopUpTransaction) transaction).setValidateStatus();

        assertEquals(TransactionStatus.FAILED.name(), transaction.getStatus());
    }

    @Test
    void testCardNumberTooLongIsRejected() {
        paymentData.put("accountNumber", "12345678901234567");

        TransactionFactory factory = TransactionFactoryProducer.getFactory(
                TransactionType.TOPUP_BALANCE.name(),
                "txn-102",
                "user-102",
                50000,
                PaymentMethod.CREDIT_CARD.name(),
                paymentData
        );

        Transaction transaction = factory.createTransaction();
        ((TopUpTransaction) transaction).setValidateStatus();

        assertEquals(TransactionStatus.FAILED.name(), transaction.getStatus());
    }

    @Test
    void testCardNumberWithLettersIsRejected() {
        paymentData.put("accountNumber", "1234abcd5678efgh");

        TransactionFactory factory = TransactionFactoryProducer.getFactory(
                TransactionType.TOPUP_BALANCE.name(),
                "txn-103",
                "user-103",
                50000,
                PaymentMethod.CREDIT_CARD.name(),
                paymentData
        );

        Transaction transaction = factory.createTransaction();
        ((TopUpTransaction) transaction).setValidateStatus();

        assertEquals(TransactionStatus.FAILED.name(), transaction.getStatus());
    }

    @Test
    void testCardNumberWithSpecialCharactersIsRejected() {
        paymentData.put("accountNumber", "1234-5678-9012-3456");

        TransactionFactory factory = TransactionFactoryProducer.getFactory(
                TransactionType.TOPUP_BALANCE.name(),
                "txn-104",
                "user-104",
                50000,
                PaymentMethod.CREDIT_CARD.name(),
                paymentData
        );

        Transaction transaction = factory.createTransaction();
        ((TopUpTransaction) transaction).setValidateStatus();

        assertEquals(TransactionStatus.FAILED.name(), transaction.getStatus());
    }

    @Test
    void testEmptyCardNumberThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentData.put("accountNumber", "");

            TransactionFactory factory = TransactionFactoryProducer.getFactory(
                    TransactionType.TOPUP_BALANCE.name(),
                    "txn-105",
                    "user-105",
                    50000,
                    PaymentMethod.CREDIT_CARD.name(),
                    paymentData
            );

            factory.createTransaction();
        });
    }

    @Test
    void testWhitespaceCardNumberThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentData.put("accountNumber", "                ");

            TransactionFactory factory = TransactionFactoryProducer.getFactory(
                    TransactionType.TOPUP_BALANCE.name(),
                    "txn-106",
                    "user-106",
                    50000,
                    PaymentMethod.CREDIT_CARD.name(),
                    paymentData
            );

            factory.createTransaction();
        });
    }

    @Test
    void testNullCardNumberThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentData.put("accountNumber", null);

            TransactionFactory factory = TransactionFactoryProducer.getFactory(
                    TransactionType.TOPUP_BALANCE.name(),
                    "txn-107",
                    "user-107",
                    50000,
                    PaymentMethod.CREDIT_CARD.name(),
                    paymentData
            );

            factory.createTransaction();
        });
    }
}