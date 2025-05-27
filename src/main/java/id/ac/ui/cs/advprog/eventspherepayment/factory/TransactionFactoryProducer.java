package id.ac.ui.cs.advprog.eventspherepayment.factory;

import id.ac.ui.cs.advprog.eventspherepayment.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class TransactionFactoryProducer {

    public static TransactionFactory getFactory(String type,
                                                UUID transactionId,
                                                UUID userId,
                                                UUID eventId,
                                                double amount,
                                                String method,
                                                Map<String, String> data) {
        if (TransactionType.TOPUP_BALANCE.getValue().equals(type)) {
            return new TopUpTransactionFactory(
                    transactionId, userId, amount, method, data
            );
        } else if (TransactionType.TICKET_PURCHASE.getValue().equals(type)) {
            return new TicketPurchaseTransactionFactory(
                    transactionId, userId, eventId, amount, method, data
            );
        }
        throw new IllegalArgumentException("Unknown transaction type: " + type);
    }

    public static TransactionFactory getFactory(String type,
                                                String transactionId,
                                                String userId,
                                                String eventId,
                                                double amount,
                                                String method,
                                                Map<String, String> data) {
        UUID transactionUuid = UUID.fromString(transactionId);
        UUID userUuid = UUID.fromString(userId);
        UUID eventUuid = null;
        if (eventId != null) {
            eventUuid = UUID.fromString(eventId);
        }


        return getFactory(type, transactionUuid, userUuid,eventUuid, amount, method, data);
    }
}