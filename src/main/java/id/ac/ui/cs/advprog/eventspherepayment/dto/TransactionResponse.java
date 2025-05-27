package id.ac.ui.cs.advprog.eventspherepayment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private UUID transactionId;
    private UUID userId;
    private String type;
    private String status;
    private double amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String method; // nullable, only filled if available
    private UUID eventId;  // nullable, only for TicketPurchase
    private Map<String, String> data; // nullable or optional, as needed
}
