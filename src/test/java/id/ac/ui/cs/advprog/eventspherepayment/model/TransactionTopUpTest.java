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

class TransactionTopUpTest {

    private Map<String, String> paymentData;
    private String userId;
    private String transactionId;
    private static final double AMOUNT = 50000.0;

    @BeforeEach
    void setUp() {
        paymentData = new HashMap<>();
        userId = UUID.randomUUID().toString();
        transactionId = UUID.randomUUID().toString();
    }

    // ---------- Bank Transfer Scenarios ----------

    @Test
    void testCreateTopUpTransactionPending_BankTransfer() {
        paymentData.put("bankName", "Bank ABC");
        paymentData.put("accountNumber", "1234567890");

        var transaction = TransactionFactoryProducer.getFactory(
                TransactionType.TOPUP_BALANCE.getValue(),
                transactionId,
                userId,
                AMOUNT,
                PaymentMethod.BANK_TRANSFER.getValue(),
                paymentData
        ).createTransaction();

        assertEquals(transactionId, transaction.getTransactionId().toString());
        assertEquals(userId, transaction.getUserId().toString());
        assertEquals(TransactionType.TOPUP_BALANCE.getValue(), transaction.getType());
        assertEquals(AMOUNT, transaction.getAmount());
        assertEquals(PaymentMethod.BANK_TRANSFER.getValue(),
                ((TopUpTransaction) transaction).getMethod());
        assertEquals(TransactionStatus.PENDING.getValue(), transaction.getStatus());
    }

    @Test
    void testInvalidAccountNumberTooShort_BankTransfer() {
        paymentData.put("bankName", "Bank XYZ");
        paymentData.put("accountNumber", "12345678");

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.BANK_TRANSFER.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    @Test
    void testInvalidAccountNumberWithLetters_BankTransfer() {
        paymentData.put("bankName", "Bank DEF");
        paymentData.put("accountNumber", "1234abc890");

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.BANK_TRANSFER.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    @Test
    void testInvalidAccountNumberWithSpecialCharacters_BankTransfer() {
        paymentData.put("bankName", "Bank HIJ");
        paymentData.put("accountNumber", "12345@7890");

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.BANK_TRANSFER.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    @Test
    void testInvalidAccountNumberTooLong_BankTransfer() {
        paymentData.put("bankName", "Bank KLM");
        paymentData.put("accountNumber", "1234567890123");

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.BANK_TRANSFER.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    @Test
    void testNullBankNameThrowsException_BankTransfer() {
        paymentData.put("bankName", null);
        paymentData.put("accountNumber", "1234567890");

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.BANK_TRANSFER.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    @Test
    void testEmptyBankNameThrowsException_BankTransfer() {
        paymentData.put("bankName", "");
        paymentData.put("accountNumber", "1234567890");

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.BANK_TRANSFER.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    @Test
    void testWhitespaceBankNameThrowsException_BankTransfer() {
        paymentData.put("bankName", "   ");
        paymentData.put("accountNumber", "1234567890");

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.BANK_TRANSFER.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    @Test
    void testNullAccountNumberThrowsException_BankTransfer() {
        paymentData.put("bankName", "Bank ABC");
        paymentData.put("accountNumber", null);

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.BANK_TRANSFER.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    @Test
    void testEmptyAccountNumberThrowsException_BankTransfer() {
        paymentData.put("bankName", "Bank DEF");
        paymentData.put("accountNumber", "");

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.BANK_TRANSFER.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    @Test
    void testWhitespaceAccountNumberThrowsException_BankTransfer() {
        paymentData.put("bankName", "Bank XYZ");
        paymentData.put("accountNumber", "          ");

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.BANK_TRANSFER.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    // ---------- Credit Card Scenarios ----------

    @Test
    void testCreateTopUpTransactionPending_CreditCard() {
        paymentData.put("accountNumber", "1234567812345678");

        var transaction = TransactionFactoryProducer.getFactory(
                TransactionType.TOPUP_BALANCE.getValue(),
                transactionId,
                userId,
                AMOUNT,
                PaymentMethod.CREDIT_CARD.getValue(),
                paymentData
        ).createTransaction();

        assertEquals(transactionId, transaction.getTransactionId().toString());
        assertEquals(userId, transaction.getUserId().toString());
        assertEquals(TransactionType.TOPUP_BALANCE.getValue(), transaction.getType());
        assertEquals(AMOUNT, transaction.getAmount());
        assertEquals(PaymentMethod.CREDIT_CARD.getValue(),
                ((TopUpTransaction) transaction).getMethod());
        assertEquals(TransactionStatus.PENDING.getValue(), transaction.getStatus());
    }

    @Test
    void testCreditCardTooShortIsRejected_CreditCard() {
        paymentData.put("accountNumber", "123456789012345");

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.CREDIT_CARD.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    @Test
    void testCreditCardTooLongIsRejected_CreditCard() {
        paymentData.put("accountNumber", "12345678901234567");

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.CREDIT_CARD.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    @Test
    void testCardNumberWithLettersIsRejected_CreditCard() {
        paymentData.put("accountNumber", "1234abcd5678efgh");

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.CREDIT_CARD.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    @Test
    void testCardNumberWithSpecialCharactersIsRejected_CreditCard() {
        paymentData.put("accountNumber", "1234-5678-9012-3456");

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.CREDIT_CARD.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    @Test
    void testEmptyCardNumberThrowsException_CreditCard() {
        paymentData.put("accountNumber", "");

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.CREDIT_CARD.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    @Test
    void testWhitespaceCardNumberThrowsException_CreditCard() {
        paymentData.put("accountNumber", "                ");

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.CREDIT_CARD.getValue(),
                        paymentData
                ).createTransaction()
        );
    }

    @Test
    void testNullCardNumberThrowsException_CreditCard() {
        paymentData.put("accountNumber", null);

        assertThrows(IllegalArgumentException.class, () ->
                TransactionFactoryProducer.getFactory(
                        TransactionType.TOPUP_BALANCE.getValue(),
                        transactionId,
                        userId,
                        AMOUNT,
                        PaymentMethod.CREDIT_CARD.getValue(),
                        paymentData
                ).createTransaction()
        );
    }
}