package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.factory;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.TicketPurchaseTransaction;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.Transaction;

import java.util.Map;

public class TicketPurchaseTransactionFactory implements TransactionFactory {
    private final String transactionId;
    private final String userId;
    private final String type;
    private final double amount;
    private final Map<String, String> ticketData;

    public TicketPurchaseTransactionFactory(String transactionId, String userId, String type,
                                            double amount, Map<String, String> ticketData) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.ticketData = ticketData;
    }

    @Override
    public Transaction createTransaction() {
        return new TicketPurchaseTransaction(transactionId, userId, type, amount, ticketData);
    }
}
