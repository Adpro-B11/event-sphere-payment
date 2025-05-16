package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.factory;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TransactionFactoryProducer {

    public static TransactionFactory getFactory(String type, String transactionId, String userId, double amount,
                                                String method, Map<String, String> data) {
        if (type.equals(TransactionType.TOPUP_BALANCE.name())) {
            return new TopUpTransactionFactory(transactionId, userId, type, method, amount, data);
        } else if (type.equals(TransactionType.TICKET_PURCHASE.name())) {
            return new TicketPurchaseTransactionFactory(transactionId, userId, type, amount, data);
        }
        throw new IllegalArgumentException("Unknown transaction type: " + type);
    }
}
