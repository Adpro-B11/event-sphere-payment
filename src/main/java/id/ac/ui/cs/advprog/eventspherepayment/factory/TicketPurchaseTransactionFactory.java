package id.ac.ui.cs.advprog.eventspherepayment.factory;

import id.ac.ui.cs.advprog.eventspherepayment.enums.TransactionType;
import id.ac.ui.cs.advprog.eventspherepayment.model.TicketPurchaseTransaction;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;

import java.util.Map;
import java.util.UUID;

public class TicketPurchaseTransactionFactory implements TransactionFactory {
    private final UUID transactionId;
    private final UUID userId;
    private final UUID eventId;
    private final double amount;
    private final String method;
    private final Map<String, String> ticketData;

    public TicketPurchaseTransactionFactory(UUID transactionId,
                                            UUID userId,
                                            UUID eventId,
                                            double amount,
                                            String method,
                                            Map<String, String> ticketData) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.eventId = eventId;
        this.amount = amount;
        this.method = method;
        this.ticketData = ticketData;
    }

    @Override
    public Transaction createTransaction() {
        return new TicketPurchaseTransaction(
                transactionId,
                userId,
                eventId,
                TransactionType.TICKET_PURCHASE.name(),
                amount,
                method,
                ticketData
        );
    }
}