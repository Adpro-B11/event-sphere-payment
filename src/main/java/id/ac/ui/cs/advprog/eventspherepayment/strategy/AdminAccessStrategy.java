package id.ac.ui.cs.advprog.eventspherepayment.strategy;

import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.repository.TransactionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AdminAccessStrategy implements AccessStrategy {
    private final TransactionRepository repository;

    public AdminAccessStrategy(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Transaction> findById(String transactionId) {
        return repository.findById(transactionId);
    }

    @Override
    public List<Transaction> viewAllTransactions() {
        return repository.findAll();
    }

    @Override
    public List<Transaction> viewUserTransactions(String userId) {
        throw new UnsupportedOperationException("User cannot view own transactions.");
    }

    @Override
    public List<Transaction> filterTransactions(String userId, String status, String type, String method, LocalDateTime createdAfter, LocalDateTime createdBefore) {
        return repository.findByFilters(userId, status, type, method, createdAfter, createdBefore);
    }

    @Override
    public void deleteTransaction(String transactionId) {
        repository.deleteById(transactionId);
    }
}
