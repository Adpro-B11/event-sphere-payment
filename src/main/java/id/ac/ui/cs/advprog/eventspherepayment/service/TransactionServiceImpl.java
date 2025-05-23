package id.ac.ui.cs.advprog.eventspherepayment.service;

import id.ac.ui.cs.advprog.eventspherepayment.enums.*;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.repository.TransactionRepository;
import id.ac.ui.cs.advprog.eventspherepayment.service.UserService;
import id.ac.ui.cs.advprog.eventspherepayment.strategy.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional          // setiap metode = satu transaksi DB
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final UserService userService;
    private AccessStrategy strategy;

    public TransactionServiceImpl(TransactionRepository repository,
                                  UserService userService) {
        this.repository   = repository;
        this.userService  = userService;
    }

    /* ---------- Strategy ---------- */

    @Override
    public void initStrategy(boolean isAdmin, String currentUserId) {
        strategy = isAdmin
                ? new AdminAccessStrategy(repository)
                : new UserAccessStrategy(repository, currentUserId);
    }

    /* ---------- CREATE ---------- */

    @Override
    public Transaction createTopUpTransaction(String userId,
                                              double amount,
                                              String method,
                                              Map<String, String> paymentData) {

        String txId = UUID.randomUUID().toString();

        Transaction tx = repository.createAndSave(
                TransactionType.TOPUP_BALANCE.getValue(), // persist ganda? → lihat catatan
                txId, userId, amount, method, paymentData
        );                                             // ① persist kali-1

        boolean success = tryAddBalance(userId, amount);
        tx.setStatus(success ? TransactionStatus.SUCCESS.getValue()
                : TransactionStatus.FAILED.getValue());

        return repository.update(tx);                  // ② merge → simpan status
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
        );                                             // ① persist kali-1

        boolean success = tryDeductBalance(userId, amount);
        tx.setStatus(success ? TransactionStatus.SUCCESS.getValue()
                : TransactionStatus.FAILED.getValue());

        return repository.update(tx);                  // ② merge → simpan status
    }

    /* ---------- READ / FILTER / DELETE ---------- */

    @Override
    public Optional<Transaction> getTransactionById(String transactionId,
                                                    String currentUserId,
                                                    boolean isAdmin) {
        return strategy.findById(transactionId);
    }

    @Override
    public List<Transaction> viewAllTransactions(String currentUserId,
                                                 boolean isAdmin) {
        return strategy.viewAllTransactions();
    }

    @Override
    public List<Transaction> filterTransactions(String currentUserId,
                                                boolean isAdmin,
                                                String status,
                                                String type,
                                                String method,
                                                LocalDateTime createdAfter,
                                                LocalDateTime createdBefore) {
        return strategy.filterTransactions(currentUserId, status, type, method,
                createdAfter, createdBefore);
    }

    @Override
    public void deleteTransaction(String transactionId, boolean isAdmin) {
        strategy.deleteTransaction(transactionId);
    }

    /* ---------- helper ---------- */

    private boolean tryAddBalance(String userId, double amount) {
        try { userService.addBalance(userId, amount); return true; }
        catch (Exception ex) { return false; }
    }

    private boolean tryDeductBalance(String userId, double amount) {
        try { userService.deductBalance(userId, amount); return true; }
        catch (Exception ex) { return false; }
    }
}
