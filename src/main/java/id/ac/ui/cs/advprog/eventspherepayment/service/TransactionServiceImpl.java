package id.ac.ui.cs.advprog.eventspherepayment.service;

import id.ac.ui.cs.advprog.eventspherepayment.client.AuthServiceClient;
import id.ac.ui.cs.advprog.eventspherepayment.enums.*;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.repository.TransactionRepository;
import id.ac.ui.cs.advprog.eventspherepayment.strategy.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final AuthServiceClient authClient;
    private final RestTemplate rest;
    private final String callbackBaseUrl;
    private AccessStrategy strategy;

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }

    private boolean currentUserIsAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ADMIN"));
    }

    @Override
    public void initStrategy() {
        boolean isAdmin = currentUserIsAdmin();
        String currentUserId = currentUserId();
        strategy = isAdmin
                ? new AdminAccessStrategy(repository)
                : new UserAccessStrategy(repository, currentUserId);
    }

    public TransactionServiceImpl(TransactionRepository repository,
                                  AuthServiceClient authClient,
                                  RestTemplate rest,
                                  @Value("${event.service.callback.url:http://localhost:8082}") String callbackBaseUrl) {
        this.repository      = repository;
        this.authClient      = authClient;
        this.rest            = rest;
        this.callbackBaseUrl = callbackBaseUrl.endsWith("/")
                ? callbackBaseUrl.substring(0, callbackBaseUrl.length() - 1)
                : callbackBaseUrl;
        initStrategy();
    }

    @Override
    public Transaction createTopUpTransaction(String userId,
                                              double amount,
                                              String method,
                                              Map<String, String> paymentData) {

        String txId = UUID.randomUUID().toString();
        Transaction tx = repository.createAndSave(
                TransactionType.TOPUP_BALANCE.getValue(),
                txId, userId, amount, method, paymentData
        );
        tx.setStatus(TransactionStatus.PENDING.getValue());
        repository.update(tx);

        processTopUpAsync(tx);
        return tx;
    }

    @Override
    public Transaction createTicketPurchaseTransaction(String userId,
                                                       double amount,
                                                       Map<String, String> ticketData) {

        String txId = UUID.randomUUID().toString();
        Transaction tx = repository.createAndSave(
                TransactionType.TICKET_PURCHASE.getValue(),
                txId, userId, amount,
                PaymentMethod.IN_APP_BALANCE.getValue(),
                ticketData
        );
        tx.setStatus(TransactionStatus.PENDING.getValue());
        repository.update(tx);

        processPurchaseAsync(tx);
        return tx;
    }

    @Override
    @Async
    public CompletableFuture<Optional<Transaction>>
    getTransactionById(String transactionId) {

        Optional<Transaction> result = strategy.findById(transactionId);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    @Async
    public CompletableFuture<List<Transaction>>
    filterTransactions(String currentUserId,
                       boolean isAdmin,
                       String status,
                       String type,
                       String method,
                       LocalDateTime createdAfter,
                       LocalDateTime createdBefore) {

        List<Transaction> list = strategy.filterTransactions(
                currentUserId, status, type, method, createdAfter, createdBefore);

        return CompletableFuture.completedFuture(list);
    }

    @Override
    @Async
    public CompletableFuture<Void> deleteTransaction(String transactionId) {
        strategy.deleteTransaction(transactionId);
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> processTopUpAsync(Transaction tx) {
        boolean success = authClient.addBalance(String.valueOf(tx.getUserId()), tx.getAmount());
        tx.setStatus(success
                ? TransactionStatus.SUCCESS.getValue()
                : TransactionStatus.FAILED.getValue());
        repository.update(tx);

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> processPurchaseAsync(Transaction tx) {
        boolean success = authClient.deductBalance(String.valueOf(tx.getUserId()), tx.getAmount());
        tx.setStatus(success
                ? TransactionStatus.SUCCESS.getValue()
                : TransactionStatus.FAILED.getValue());
        repository.update(tx);

        if (success) {
            sendCallback(tx);
        }
        return CompletableFuture.completedFuture(null);
    }

    private void sendCallback(Transaction tx) {
        String callbackUrl = callbackBaseUrl + "/webhook/transaction/purchase-success";
        Map<String, Object> payload = Map.of(
                "transactionId", tx.getTransactionId(),
                "userId",        tx.getUserId(),
                "data",          tx.getData()
        );
        try {
            rest.postForEntity(callbackUrl, payload, Void.class);
        } catch (Exception ignored) {

        }
    }
}
