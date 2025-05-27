package id.ac.ui.cs.advprog.eventspherepayment.service;

import id.ac.ui.cs.advprog.eventspherepayment.client.AuthServiceClient;
import id.ac.ui.cs.advprog.eventspherepayment.client.TicketServiceClient;
import id.ac.ui.cs.advprog.eventspherepayment.dto.TransactionMapper;
import id.ac.ui.cs.advprog.eventspherepayment.dto.TransactionResponse;
import id.ac.ui.cs.advprog.eventspherepayment.enums.*;
import id.ac.ui.cs.advprog.eventspherepayment.model.TicketPurchaseTransaction;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final AuthServiceClient     authClient;
    private final TicketServiceClient   ticketServiceClient;
    private final String                callbackBaseUrl;
    private       AccessStrategy        strategy;

    public TransactionServiceImpl(TransactionRepository repository,
                                  AuthServiceClient authClient,
                                  TicketServiceClient ticketServiceClient,
                                  @Value("${event.service.base-url}") String callbackBaseUrl) {
        this.repository      = repository;
        this.authClient      = authClient;
        this.ticketServiceClient = ticketServiceClient;
        this.callbackBaseUrl = callbackBaseUrl.endsWith("/")
                ? callbackBaseUrl.substring(0, callbackBaseUrl.length() - 1)
                : callbackBaseUrl;
    }

    public String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (auth != null) ? auth.getName() : null;
        return userId;
    }

    public boolean currentUserIsAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ADMIN") || a.equals("ROLE_ADMIN"));
        return isAdmin;
    }

    @Override
    public String initStrategy() {
        boolean isAdmin      = currentUserIsAdmin();
        String  currentUser  = currentUserId();
        strategy = isAdmin
                ? new AdminAccessStrategy(repository)
                : new UserAccessStrategy(repository, currentUser);
        return currentUser;
    }

    @Override
    public Transaction createTopUpTransaction(String userId,
                                              double amount,
                                              String method,
                                              Map<String, String> paymentData) {
        initStrategy();

        String txId = UUID.randomUUID().toString();
        Transaction tx = repository.createAndSave(
                TransactionType.TOPUP_BALANCE.getValue(),
                txId, userId, null, amount, method, paymentData
        );
        tx.setStatus(TransactionStatus.PENDING.getValue());
        repository.update(tx);

        processTopUpAsync(tx);
        return tx;
    }

    @Async
    public CompletableFuture<Void> processTopUpAsync(Transaction tx) {
        initStrategy();

        boolean success = authClient.addBalance(String.valueOf(tx.getUserId()), tx.getAmount());
        tx.setStatus(success ? TransactionStatus.SUCCESS.getValue()
                : TransactionStatus.FAILED.getValue());
        repository.update(tx);

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Transaction createTicketPurchaseTransaction(String userId,
                                                       String eventId,
                                                       double amount,
                                                       Map<String, String> ticketData) {
        initStrategy();

        String txId = UUID.randomUUID().toString();
        Transaction tx = repository.createAndSave(
                TransactionType.TICKET_PURCHASE.getValue(),
                txId, userId, eventId, amount,
                PaymentMethod.IN_APP_BALANCE.getValue(),
                ticketData
        );
        tx.setStatus(TransactionStatus.PENDING.getValue());
        repository.update(tx);

        processPurchaseAsync(tx, getEventIdFromTransaction(tx));
        return tx;
    }

    private String getEventIdFromTransaction(Transaction tx) {
        if (tx instanceof TicketPurchaseTransaction ticketPurchaseTx) {
            return String.valueOf(ticketPurchaseTx.getEventId());
        }
        return null;
    }

    @Async
    public CompletableFuture<Void> processPurchaseAsync(Transaction tx, String eventId) {
        initStrategy();

        boolean balanceSuccess = authClient.deductBalance(String.valueOf(tx.getUserId()), tx.getAmount());

        if (!balanceSuccess) {
            tx.setStatus(TransactionStatus.FAILED.getValue());
            repository.update(tx);
            return CompletableFuture.completedFuture(null);
        }

        // Deduct tickets (bisa batch/loop sesuai implementasi client)
        boolean ticketSuccess = ticketServiceClient.deductTickets(tx.getData(), eventId);
        if (!ticketSuccess) {
            tx.setStatus(TransactionStatus.FAILED.getValue());
            repository.update(tx);
            authClient.addBalance(String.valueOf(tx.getUserId()), tx.getAmount());
            return CompletableFuture.completedFuture(null);
        }

        tx.setStatus(TransactionStatus.SUCCESS.getValue());
        repository.update(tx);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Optional<TransactionResponse>> getTransactionById(String transactionId) {
        initStrategy();
        Optional<Transaction> result = strategy.findById(transactionId);
        Optional<TransactionResponse> dtoResult = result.map(TransactionMapper::toDto);
        return CompletableFuture.completedFuture(dtoResult);
    }

    @Async
    public CompletableFuture<List<TransactionResponse>> filterTransactions(
            String currentUserId,
            String status,
            String type,
            String method,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore) {

        initStrategy();

        if (strategy instanceof AdminAccessStrategy) {
            currentUserId = null;
        }
        List<Transaction> entityList = strategy.filterTransactions(
                currentUserId, status, type, method, createdAfter, createdBefore
        );

        List<TransactionResponse> dtoList = entityList.stream()
                .map(TransactionMapper::toDto)
                .toList();

        return CompletableFuture.completedFuture(dtoList);
    }

    @Override
    @Async
    public CompletableFuture<Void> deleteTransaction(String transactionId) {
        initStrategy();
        strategy.deleteTransaction(transactionId);
        return CompletableFuture.completedFuture(null);
    }
}
