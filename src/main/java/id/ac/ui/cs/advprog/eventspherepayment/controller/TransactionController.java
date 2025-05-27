package id.ac.ui.cs.advprog.eventspherepayment.controller;

import id.ac.ui.cs.advprog.eventspherepayment.dto.*;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping(
            value = "/topup",
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
            value = "/purchase/{eventId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Transaction> purchaseTicket(@PathVariable String eventId, @RequestBody PurchaseRequest request) {

        // Validation: amount must be positive, and all ticket quantities must be positive integers
        if (request.getAmount() < 0) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getTicketData() == null || request.getTicketData().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        for (Map.Entry<String, String> entry : request.getTicketData().entrySet()) {
            try {
                int qty = Integer.parseInt(entry.getValue());
                if (qty < 0) {
                    return ResponseEntity.badRequest().build();
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        String currentUserId = request.getUserId();

        Transaction tx = service.createTicketPurchaseTransaction(
                currentUserId,
                request.getEventId(),
                request.getAmount(),
                request.getTicketData()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<GetByIdResponse>> getById(@PathVariable String id) {

        service.initStrategy();

        return service.getTransactionById(id)
                .thenApply(opt -> opt
                        .map(tx -> ResponseEntity.ok(new GetByIdResponse(tx)))
                        .orElseGet(() -> ResponseEntity.notFound().build())
                );
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<FilterTransactionsResponse>> filteringTransaction(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String method,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore) {

        // Selalu reset strategy berdasarkan user yang lagi login
        String currentUserId = service.initStrategy();

        return service.filterTransactions(
                        currentUserId,  status, type, method,
                        createdAfter, createdBefore)
                .thenApply(dtoList -> ResponseEntity.ok(new FilterTransactionsResponse(dtoList)));
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> delete(@PathVariable String id) {

        return service.deleteTransaction(id)
                .thenApply(v -> ResponseEntity.ok().build());
    }
}
