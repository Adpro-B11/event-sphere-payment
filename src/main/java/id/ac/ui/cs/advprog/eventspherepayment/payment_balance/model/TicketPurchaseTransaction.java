package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.TransactionStatus;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.TransactionType;
import lombok.Getter;

import java.util.Map;

@Getter
public class TicketPurchaseTransaction extends Transaction {

    private final Map<String, String> ticketData;

    public TicketPurchaseTransaction(String transactionId, String userId, String type, double amount, Map<String, String> ticketData) {
        super(transactionId, userId, type, TransactionStatus.FAILED.name(), amount);

        if (!TransactionType.contains(type)) {
            throw new IllegalArgumentException("Invalid transaction type.");
        }

        validateTicketData(ticketData);

        this.ticketData = ticketData;
    }

    private void validateTicketData(Map<String, String> ticketData) {
        if (ticketData == null || ticketData.isEmpty()) {
            throw new IllegalArgumentException("Ticket data must not be null or empty.");
        }

        for (Map.Entry<String, String> entry : ticketData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key == null || key.trim().isEmpty()) {
                throw new IllegalArgumentException("Ticket type must not be null, empty, or whitespace.");
            }

            int quantity;
            try {
                quantity = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Ticket amount must be a valid integer.");
            }

            if (quantity <= 0) {
                throw new IllegalArgumentException("Ticket amount must be greater than 0.");
            }
        }
    }

    @Override
    public void validateTransaction() {
        setStatus(TransactionStatus.SUCCESS.name());
    }

    public void setValidateStatus() {
        validateTransaction();
    }
}
