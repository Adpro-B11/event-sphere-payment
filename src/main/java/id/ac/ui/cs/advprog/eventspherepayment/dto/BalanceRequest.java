package id.ac.ui.cs.advprog.eventspherepayment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceRequest {
    private String userId;
    private double amount;
}