package id.ac.ui.cs.advprog.eventspherepayment.controller;

import id.ac.ui.cs.advprog.eventspherepayment.dto.*;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.service.TransactionService;
import lombok.extern.slf4j.Slf4j;                     // 👈 ➊
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j                                               // 👈 ➋
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    /* ─────────────────────────── TOP-UP ─────────────────────────── */

    @PostMapping(
            value = "/topup",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Transaction> topUpBalance(@RequestBody TopUpRequest request) {

        log.info("⏩  Top-Up request  userId={} amount={} method={}",
                request.getUserId(), request.getAmount(), request.getMethod());

        service.initStrategy();
        Transaction tx = service.createTopUpTransaction(
                request.getUserId(),
                request.getAmount(),
                request.getMethod(),
                request.getPaymentData()
        );

        log.info("✅  Top-Up created id={} status={}", tx.getId(), tx.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    /* ──────────────────────── PURCHASE TICKET ───────────────────── */

    @PostMapping(
            value = "/purchase/{eventId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Transaction> purchaseTicket(@PathVariable String eventId, @RequestBody PurchaseRequest request) {

        log.info("⏩  Purchase request  eventId={} ticketData={} amount={}",
                request.getEventId(), request.getTicketData(), request.getAmount());

        // Validation: amount must be positive, and all ticket quantities must be positive integers
        if (request.getAmount() < 0) {
            log.warn("⛔  Reject purchase: negative amount");
            return ResponseEntity.badRequest().build();
        }
        if (request.getTicketData() == null || request.getTicketData().isEmpty()) {
            log.warn("⛔  Reject purchase: no ticket data provided");
            return ResponseEntity.badRequest().build();
        }
        for (Map.Entry<String, String> entry : request.getTicketData().entrySet()) {
            try {
                int qty = Integer.parseInt(entry.getValue());
                if (qty < 0) {
                    log.warn("⛔  Reject purchase: ticket {} has negative quantity ({})", entry.getKey(), qty);
                    return ResponseEntity.badRequest().build();
                }
            } catch (NumberFormatException e) {
                log.warn("⛔  Reject purchase: ticket {} has invalid quantity '{}'", entry.getKey(), entry.getValue());
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

        log.info("✅  Purchase created id={} status={}", tx.getId(), tx.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    /* ───────────────────────────── GET BY ID ────────────────────── */

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<GetByIdResponse>> getById(@PathVariable String id) {

        log.debug("➡  Get transaction by id={}", id);
        service.initStrategy();

        return service.getTransactionById(id)
                .thenApply(opt -> opt
                        .map(tx -> {
                            log.debug("✅  Found transaction id={}", id);
                            return ResponseEntity.ok(new GetByIdResponse(tx));
                        })
                        .orElseGet(() -> {
                            log.warn("❓  Transaction id={} not found", id);
                            return ResponseEntity.notFound().build();
                        })
                );
    }

    /* ─────────────────── FILTERING / SEARCH LIST ────────────────── */

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

        log.debug("➡  Filter tx  userId={}  status={} type={} method={} after={} before={}",
                currentUserId, status, type, method, createdAfter, createdBefore);

        return service.filterTransactions(
                        currentUserId,  status, type, method,
                        createdAfter, createdBefore)
                .thenApply(dtoList -> {
                    log.debug("✅  Filter result: {} records", dtoList.size());
                    return ResponseEntity.ok(new FilterTransactionsResponse(dtoList));
                });
    }


    /* ───────────────────────────── DELETE ───────────────────────── */

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> delete(@PathVariable String id) {

        log.info("🗑  Delete transaction id={}", id);
        return service.deleteTransaction(id)
                .thenApply(v -> {
                    log.info("✅  Deleted transaction id={}", id);
                    return ResponseEntity.ok().build();
                });
    }
}
