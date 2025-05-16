package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.PaymentMethod;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.TransactionStatus;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.TransactionType;
import lombok.Getter;

import java.util.Map;

@Getter
public class TopUpTransaction extends Transaction {

    private String method;
    private Map<String, String> paymentData;

    public TopUpTransaction(String transactionId, String userId, String type, String method, double amount, Map<String, String> paymentData) {
        super(transactionId, userId, type, TransactionStatus.FAILED.name(), amount);

        if (!TransactionType.contains(type)) {
            throw new IllegalArgumentException("Invalid transaction type.");
        }

        if (!PaymentMethod.contains(method)) {
            throw new IllegalArgumentException("Invalid payment method.");
        }

        validateInput(method, paymentData);

        this.method = method;
        this.paymentData = paymentData;
    }

    private void validateInput(String method, Map<String, String> paymentData) {
        String accountNumber = paymentData.get("accountNumber");

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number must not be null or empty.");
        }

        if (method.equals(PaymentMethod.BANK_TRANSFER.name())) {
            String bankName = paymentData.get("bankName");
            if (bankName == null || bankName.trim().isEmpty()) {
                throw new IllegalArgumentException("Bank name must not be null or empty for bank transfers.");
            }
        }
    }

    @Override
    public void validateTransaction() {
        String accountNumber = paymentData.get("accountNumber");

        if (method.equals(PaymentMethod.BANK_TRANSFER.name())) {
            if (accountNumber.length() != 10 || !accountNumber.matches("\\d+")) {
                setStatus(TransactionStatus.FAILED.name());
                return;
            }
        } else if (method.equals(PaymentMethod.CREDIT_CARD.name())) {
            if (accountNumber.length() != 16 || !accountNumber.matches("\\d+")) {
                setStatus(TransactionStatus.FAILED.name());
                return;
            }
        }

        setStatus(TransactionStatus.SUCCESS.name());
    }

    public void setValidateStatus() {
        validateTransaction();
    }
}
