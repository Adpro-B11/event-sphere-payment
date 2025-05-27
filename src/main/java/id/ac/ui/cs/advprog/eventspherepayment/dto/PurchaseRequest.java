package id.ac.ui.cs.advprog.eventspherepayment.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {
    private String userId;
    private double amount;
    private String eventId;
    private Map<String, String> ticketData;
}
