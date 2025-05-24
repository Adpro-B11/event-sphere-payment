package id.ac.ui.cs.advprog.eventspherepayment.model;

import id.ac.ui.cs.advprog.eventspherepayment.enums.PaymentMethod;
import id.ac.ui.cs.advprog.eventspherepayment.enums.TransactionStatus;
import id.ac.ui.cs.advprog.eventspherepayment.enums.TransactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import jakarta.persistence.*;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@DiscriminatorValue("TOPUP_BALANCE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TopUpTransaction extends Transaction {

    @Column(name = "payment_method")
    private String method;

    @ElementCollection
    @CollectionTable(
            name = "payment_data",
            joinColumns = @JoinColumn(name = "transaction_id")
    )
    @MapKeyColumn(name = "data_key")
    @Column(name = "data_value")
    private Map<String, String> paymentData;

    public TopUpTransaction(UUID transactionId,
                            UUID userId,
                            String type,
                            double amount,
                            String method,
                            Map<String, String> paymentData) {
        super(transactionId, userId, type, amount);

        if (!Objects.equals(type, TransactionType.TOPUP_BALANCE.getValue())) {
            throw new IllegalArgumentException("Invalid transaction type.");
        }

        if (Objects.equals(method, PaymentMethod.IN_APP_BALANCE.getValue())) {
            throw new IllegalArgumentException("Invalid payment method: " + method);
        }

        validateTransaction(method, paymentData);

        this.method = method;
        this.paymentData = paymentData;
        setStatus(TransactionStatus.PENDING.name());
    }

    @Override
    public void validateTransaction(String method, Map<String, String> paymentData) {
        String accountNumber = paymentData.get("accountNumber");

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number must not be null or empty.");
        }

        if (method.equals(PaymentMethod.BANK_TRANSFER.name())) {
            String bankName = paymentData.get("bankName");
            if (bankName == null || bankName.trim().isEmpty()) {
                throw new IllegalArgumentException("Bank name must not be null or empty for bank transfers.");
            }
            if (accountNumber.length() != 10 || !accountNumber.matches("\\d+")) {
                throw new IllegalArgumentException("Bank account number format is incorrect.");
            }
        } else if (method.equals(PaymentMethod.CREDIT_CARD.name())) {
            if (accountNumber.length() != 16 || !accountNumber.matches("\\d+")) {
                throw new IllegalArgumentException("Credit Card number format is incorrect.");
            }
        }
    }

    @Override
    public Map<String, String> getData() {
        return paymentData;
    }
}