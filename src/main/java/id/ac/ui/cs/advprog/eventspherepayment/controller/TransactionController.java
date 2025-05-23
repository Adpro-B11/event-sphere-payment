package id.ac.ui.cs.advprog.eventspherepayment.controller;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.service.TransactionService;
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

    /* ---------- POST ---------- */

    @PostMapping("/topup")
    public ResponseEntity<Transaction> topUpBalance(
            @RequestParam String userId,
            @RequestParam double amount,
            @RequestParam String method,
            @RequestBody(required = false) Map<String,String> paymentData) {

        Transaction tx = service.createTopUpTransaction(userId, amount, method, paymentData);
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @PostMapping("/purchase")
    public ResponseEntity<Transaction> purchaseTicket(
            @RequestParam String userId,
            @RequestParam double amount,
            @RequestBody Map<String,String> ticketData) {

        Transaction tx = service.createTicketPurchaseTransaction(userId, amount, ticketData);
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    /* ---------- GET ---------- */

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getById(
            @PathVariable String id,
            @RequestParam String currentUserId,
            @RequestParam(defaultValue="false") boolean isAdmin) {

        service.initStrategy(isAdmin, currentUserId);
        return service.getTransactionById(id, currentUserId, isAdmin)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> list(
            @RequestParam String currentUserId,
            @RequestParam(defaultValue="false") boolean isAdmin,
            @RequestParam(required=false) String status,
            @RequestParam(required=false) String type,
            @RequestParam(required=false) String method,
            @RequestParam(required=false)
            @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(required=false)
            @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore) {

        service.initStrategy(isAdmin, currentUserId);
        List<Transaction> list = service.filterTransactions(
                currentUserId, isAdmin, status, type, method, createdAfter, createdBefore);
        return ResponseEntity.ok(list);
    }

    /* ---------- DELETE ---------- */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @RequestParam String currentUserId,
            @RequestParam(defaultValue="false") boolean isAdmin) {

        service.initStrategy(isAdmin, currentUserId);
        service.deleteTransaction(id, isAdmin);
        return ResponseEntity.noContent().build();
    }
}
