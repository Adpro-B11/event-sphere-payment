package id.ac.ui.cs.advprog.eventspherepayment.strategy;

import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public class UserAccessStrategy implements AccessStrategy {
    private final TransactionRepository repository;
    private final String currentUserId;

    public UserAccessStrategy(TransactionRepository repository, String currentUserId) {
        this.repository = repository;
        this.currentUserId = currentUserId;
    }

    @Override
    public Optional<Transaction> findById(String transactionId) {
        return repository.findById(transactionId)
                .filter(t -> t.getUserId().toString().equals(currentUserId));
    }

    @Override
    public List<Transaction> viewAllTransactions() {
        throw new UnsupportedOperationException("User cannot view all transactions.");
    }

    @Override
    public List<Transaction> viewUserTransactions(String userId) {
        return repository.findByFilters(currentUserId, null, null, null, null, null);
    }

    @Override
    public List<Transaction> filterTransactions(String userId,
                                                String status,
                                                String type,
                                                String method,
                                                LocalDateTime createdAfter,
                                                LocalDateTime createdBefore) {
        return repository.findByFilters(currentUserId, status, type, method, createdAfter, createdBefore);
    }

    @Override
    public void deleteTransaction(String transactionId) {
        throw new UnsupportedOperationException("User cannot delete transactions.");
    }
}
