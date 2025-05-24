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
@DiscriminatorValue("TICKET_PURCHASE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketPurchaseTransaction extends Transaction {

    @Column(name = "payment_method")
    private String method;

    @ElementCollection
    @CollectionTable(
            name = "ticket_data",
            joinColumns = @JoinColumn(name = "transaction_id")
    )
    @MapKeyColumn(name = "ticket_type")
    @Column(name = "quantity")
    private Map<String, String> ticketData;

    public TicketPurchaseTransaction(UUID transactionId,
                                     UUID userId,
                                     String type,
                                     double amount,
                                     String method,
                                     Map<String, String> ticketData) {
        super(transactionId, userId, type, amount);

        if (!Objects.equals(type, TransactionType.TICKET_PURCHASE.name())) {
            throw new IllegalArgumentException("Invalid transaction type: " + type);
        }

        if (!Objects.equals(method, PaymentMethod.IN_APP_BALANCE.name())) {
            throw new IllegalArgumentException("Invalid payment method: " + method);
        }

        validateTransaction(method, ticketData);

        this.method = method;
        this.ticketData = ticketData;
        setStatus(TransactionStatus.PENDING.name());
    }

    @Override
    public void validateTransaction(String method, Map<String, String> ticketData) {
        if (ticketData == null || ticketData.isEmpty()) {
            throw new IllegalArgumentException("Ticket data must not be null or empty.");
        }

        for (Map.Entry<String, String> entry : ticketData.entrySet()) {
            int quantity = getQuantity(entry);

            if (quantity <= 0) {
                throw new IllegalArgumentException("Ticket amount must be greater than 0.");
            }
        }
    }

    private static int getQuantity(Map.Entry<String, String> entry) {
        String ticketType = entry.getKey();
        String amountStr = entry.getValue();

        if (ticketType == null || ticketType.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket type must not be null or empty.");
        }

        int quantity;
        try {
            quantity = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ticket amount must be a valid integer.");
        }
        return quantity;
    }

    @Override
    public Map<String, String> getData() {
        return ticketData;
    }
}