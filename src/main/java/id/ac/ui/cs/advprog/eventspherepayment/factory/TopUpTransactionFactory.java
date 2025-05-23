package id.ac.ui.cs.advprog.eventspherepayment.factory;

import id.ac.ui.cs.advprog.eventspherepayment.enums.TransactionType;
import id.ac.ui.cs.advprog.eventspherepayment.model.TopUpTransaction;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;

import java.util.Map;
import java.util.UUID;

public class TopUpTransactionFactory implements TransactionFactory {
    private final UUID transactionId;
    private final UUID userId;
    private final double amount;
    private final String method;
    private final Map<String, String> paymentData;

    public TopUpTransactionFactory(UUID transactionId,
                                   UUID userId,
                                   double amount,
                                   String method,
                                   Map<String, String> paymentData) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.amount = amount;
        this.method = method;
        this.paymentData = paymentData;
    }

    @Override
    public Transaction createTransaction() {
        return new TopUpTransaction(
                transactionId,
                userId,
                TransactionType.TOPUP_BALANCE.getValue(),
                amount,
                method,
                paymentData
        );
    }
}