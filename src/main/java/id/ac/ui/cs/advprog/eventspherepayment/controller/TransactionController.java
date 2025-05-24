package id.ac.ui.cs.advprog.eventspherepayment.controller;

import id.ac.ui.cs.advprog.eventspherepayment.dto.PurchaseRequest;
import id.ac.ui.cs.advprog.eventspherepayment.dto.TopUpRequest;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

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
        Transaction tx = service.createTopUpTransaction(
                request.getUserId(),
                request.getAmount(),
                request.getMethod(),
                request.getPaymentData()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @PostMapping(value = "/purchase",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Transaction> purchaseTicket(@RequestBody PurchaseRequest request) {
        Transaction tx = service.createTicketPurchaseTransaction(
                request.getUserId(),
                request.getAmount(),
                request.getTicketData()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getById(
            @PathVariable String id
    ) {
        service.initStrategy();
        return service.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> list(
            @RequestParam String currentUserId,
            @RequestParam(defaultValue = "false") boolean isAdmin,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String method,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore
    ) {
        service.initStrategy();
        List<Transaction> list = service.filterTransactions(
                currentUserId, isAdmin, status, type, method, createdAfter, createdBefore);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @RequestParam String currentUserId,
            @RequestParam(defaultValue = "false") boolean isAdmin
    ) {
        service.initStrategy();
        service.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

}
