package id.ac.ui.cs.advprog.eventspherepayment.dto;

import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterTransactionsResponse {
    private List<Transaction> data;
}

