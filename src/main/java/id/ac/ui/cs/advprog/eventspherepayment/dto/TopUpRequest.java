package id.ac.ui.cs.advprog.eventspherepayment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class TopUpRequest {
    private String userId;
    private double amount;
    private String method;
    private Map<String, String> paymentData;
}

