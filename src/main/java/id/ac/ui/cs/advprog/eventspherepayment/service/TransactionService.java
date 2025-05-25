package id.ac.ui.cs.advprog.eventspherepayment.service;

import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface TransactionService {

    void initStrategy();

    Transaction createTopUpTransaction(
            String userId,
            double amount,
            String method,
            Map<String, String> paymentData
    );

    Transaction createTicketPurchaseTransaction(
            String userId,
            double amount,
            Map<String, String> ticketData
    );

    CompletableFuture<Optional<Transaction>> getTransactionById(
            String transactionId
    );

    CompletableFuture<List<Transaction>> filterTransactions(
            String currentUserId,
            boolean isAdmin,
            String status,
            String type,
            String method,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore
    );

    CompletableFuture<Void>  deleteTransaction(
            String transactionId
    );
}
