package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.factory;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.TopUpTransaction;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.Transaction;

import java.util.Map;

public class TopUpTransactionFactory implements TransactionFactory {
    private final String transactionId;
    private final String userId;
    private final String type;
    private final String method;
    private final double amount;
    private final Map<String, String> paymentData;

    public TopUpTransactionFactory(String transactionId, String userId, String type,
                                   String method, double amount, Map<String, String> paymentData) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.type = type;
        this.method = method;
        this.amount = amount;
        this.paymentData = paymentData;
    }

    @Override
    public Transaction createTransaction() {
        return new TopUpTransaction(transactionId, userId, type, method, amount, paymentData);
    }
}
