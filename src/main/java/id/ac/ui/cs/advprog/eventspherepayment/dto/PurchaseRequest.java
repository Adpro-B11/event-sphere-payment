package id.ac.ui.cs.advprog.eventspherepayment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PurchaseRequest {
    @NotBlank            private String ticketId;
    @NotBlank            private String eventId;
    @Min(value = 0, message = "amount must be non-negative")
    private double amount;
    @Min(value = 0, message = "quantity must be non-negative")
    private int quantity;
    @NotBlank            private String paymentMethod;

    @Builder
    public PurchaseRequest(String ticketId,
                           String eventId,
                           double amount,
                           int quantity,
                           String paymentMethod) {
        this.ticketId     = ticketId;
        this.eventId      = eventId;
        this.amount       = amount;
        this.quantity     = quantity;
        this.paymentMethod= paymentMethod;
    }
}
