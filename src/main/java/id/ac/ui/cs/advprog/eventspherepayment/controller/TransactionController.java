package id.ac.ui.cs.advprog.eventspherepayment.controller;

import id.ac.ui.cs.advprog.eventspherepayment.dto.FilterTransactionsResponse;
import id.ac.ui.cs.advprog.eventspherepayment.dto.GetByIdResponse;
import id.ac.ui.cs.advprog.eventspherepayment.dto.PurchaseRequest;
import id.ac.ui.cs.advprog.eventspherepayment.dto.TopUpRequest;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping(value = "/topup",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Transaction> topUpBalance(@RequestBody TopUpRequest request) {
        service.initStrategy();
        Transaction tx = service.createTopUpTransaction(
                request.getUserId(),
                request.getAmount(),
                request.getMethod(),
                request.getPaymentData()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @PostMapping(
            value = "/purchase",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Transaction> purchaseTicket(@RequestBody PurchaseRequest request) {

        if (request.getAmount() < 0 || request.getQuantity() < 0) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }

        String currentUserId = service.initStrategy();
        Map<String, String> ticketData = new HashMap<>();
        ticketData.put(
                request.getTicketId(),
                String.valueOf(request.getQuantity())
        );

        Transaction tx = service.createTicketPurchaseTransaction(
                currentUserId,
                request.getEventId(),
                request.getAmount(),
                ticketData
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tx);
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<GetByIdResponse>> getById(
            @PathVariable String id) {
        service.initStrategy();

        return service.getTransactionById(id)
                .thenApply(opt -> opt
                        .map(tx -> ResponseEntity.ok(new GetByIdResponse(tx)))
                        .orElseGet(() -> ResponseEntity.notFound().build())
                );
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<FilterTransactionsResponse>> filteringTransaction(
            @RequestParam String currentUserId,
            @RequestParam(defaultValue = "false") boolean isAdmin,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String method,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore) {

        return service.filterTransactions(
                        currentUserId, isAdmin, status, type, method,
                        createdAfter, createdBefore)
                .thenApply(list -> ResponseEntity.ok(new FilterTransactionsResponse(list)));
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> delete(
            @PathVariable String id){
        return service.deleteTransaction(id).thenApply(v -> ResponseEntity.ok().build());
    }

}
