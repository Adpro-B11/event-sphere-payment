package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.controller;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums.TransactionType;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.service.AccessStrategy;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.service.AdminAccessStrategy;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.service.TransactionService;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.service.UserAccessStrategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {


    private final TransactionService transactionService;
    private final AdminAccessStrategy adminAccessStrategy;
    private final UserAccessStrategy userAccessStrategy;

    @Autowired
    public TransactionController(TransactionService transactionService,
                                 AdminAccessStrategy adminAccessStrategy,
                                 UserAccessStrategy userAccessStrategy) {
        this.transactionService = transactionService;
        this.adminAccessStrategy = adminAccessStrategy;
        this.userAccessStrategy = userAccessStrategy;
    }

    // Temporary method to determine if user is admin - will be replaced with proper auth later
    private boolean isAdmin(String userId) {
        // For simplicity, consider user ID starting with "admin" as admin
        return userId != null && userId.startsWith("admin");
    }

    private void setAppropriateStrategy(String userId) {
        AccessStrategy strategy = isAdmin(userId) ?
                adminAccessStrategy : userAccessStrategy;
        transactionService.setStrategy(strategy);
    }

    @PostMapping("/topup")
    public ResponseEntity<?> topUpBalance(@RequestHeader("User-Id") String userId,
                                          @RequestBody Map<String, Object> requestBody) {
        try {
            double amount = Double.parseDouble(requestBody.get("amount").toString());
            String method = requestBody.get("method").toString();
            Map<String, String> data = new HashMap<>();

            // Add additional data if provided
            if (requestBody.containsKey("data") && requestBody.get("data") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> additionalData = (Map<String, String>) requestBody.get("data");
                data.putAll(additionalData);
            }

            setAppropriateStrategy(userId);

            String transactionId = UUID.randomUUID().toString();
            transactionService.createTransaction(
                    TransactionType.TOPUP_BALANCE.name(),
                    transactionId,
                    userId,
                    amount,
                    method,
                    data
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactionId", transactionId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/purchase-ticket")
    public ResponseEntity<?> purchaseTicket(@RequestHeader("User-Id") String userId,
                                            @RequestBody Map<String, Object> requestBody) {
        try {
            double amount = Double.parseDouble(requestBody.get("amount").toString());
            String eventId = requestBody.get("eventId").toString();

            Map<String, String> data = new HashMap<>();
            data.put("eventId", eventId);

            // Add additional data if provided
            if (requestBody.containsKey("data") && requestBody.get("data") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> additionalData = (Map<String, String>) requestBody.get("data");
                data.putAll(additionalData);
            }

            setAppropriateStrategy(userId);

            String transactionId = UUID.randomUUID().toString();
            transactionService.createTransaction(
                    TransactionType.TICKET_PURCHASE.name(),
                    transactionId,
                    userId,
                    amount,
                    "BALANCE", // Assuming tickets are purchased using balance
                    data
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactionId", transactionId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Insufficient balance");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserTransactions(@RequestHeader("User-Id") String userId) {
        try {
            setAppropriateStrategy(userId);
            List<Transaction> transactions = transactionService.viewUserTransactions(userId);

            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/user/filter")
    public ResponseEntity<?> filterUserTransactionsByType(
            @RequestHeader("User-Id") String userId,
            @RequestParam(required = false) String type) {
        try {
            setAppropriateStrategy(userId);
            List<Transaction> transactions = transactionService.filterTransactionsByType(userId, type);

            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTransactions(@RequestHeader("User-Id") String userId) {
        try {
            if (!isAdmin(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        Map.of("success", false, "message", "Access denied")
                );
            }

            setAppropriateStrategy(userId);
            List<Transaction> transactions = transactionService.viewAllTransactions();

            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterTransactions(
            @RequestHeader("User-Id") String adminId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        try {
            if (!isAdmin(adminId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        Map.of("success", false, "message", "Access denied")
                );
            }

            setAppropriateStrategy(adminId);
            List<Transaction> transactions = transactionService.filterTransactions(userId, status, type);

            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<?> deleteTransaction(
            @RequestHeader("User-Id") String userId,
            @PathVariable String transactionId) {
        try {
            if (!isAdmin(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        Map.of("success", false, "message", "Access denied")
                );
            }

            setAppropriateStrategy(userId);
            transactionService.deleteTransaction(transactionId);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}