package id.ac.ui.cs.advprog.eventspherepayment.dto;

import java.util.Map;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class DeductTicketRequest {
    @NotEmpty
    private Map<String, String> tickets;
    private String eventId;
}