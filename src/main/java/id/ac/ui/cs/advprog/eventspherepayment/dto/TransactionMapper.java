package id.ac.ui.cs.advprog.eventspherepayment.dto;

import id.ac.ui.cs.advprog.eventspherepayment.model.TicketPurchaseTransaction;
import id.ac.ui.cs.advprog.eventspherepayment.model.TopUpTransaction;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;

import java.util.Map;
import java.util.UUID;

public class TransactionMapper {
    public static TransactionResponse toDto(Transaction tx) {
        String method = null;
        UUID eventId = null;
        Map<String, String> data = null;

        if (tx instanceof TopUpTransaction t) {
            method = t.getMethod();
            data = t.getData() == null ? null : Map.copyOf(t.getData()); // <- Copy map!
        } else if (tx instanceof TicketPurchaseTransaction t) {
            method = t.getMethod();
            eventId = t.getEventId();
            data = t.getData() == null ? null : Map.copyOf(t.getData()); // <- Copy map!
        } else if (tx.getData() != null) {
            data = Map.copyOf(tx.getData()); // <- Copy map!
        }

        return new TransactionResponse(
                tx.getTransactionId(),
                tx.getUserId(),
                tx.getType(),
                tx.getStatus(),
                tx.getAmount(),
                tx.getCreatedAt(),
                tx.getUpdatedAt(),
                method,
                eventId,
                data
        );
    }
}

