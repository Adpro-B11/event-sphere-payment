package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.service;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminAccessStrategy implements AccessStrategy {
    private final TransactionRepository transactionRepo;

    public AdminAccessStrategy(TransactionRepository transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    @Override
    public void createTransaction(Transaction transaction) {
        throw new UnsupportedOperationException("Admin cannot create transactions");
    }

    @Override
    public List<Transaction> viewUserTransactions(String userId){
        throw new UnsupportedOperationException("Admin cannot view own transactions");
    }

    @Override
    public void deleteTransaction(String transactionId) {
        transactionRepo.deleteById(transactionId);
    }

    @Override
    public List<Transaction> viewAllTransactions() {
        return transactionRepo.findAll();
    }

    @Override
    public List<Transaction> filterTransactions(String userId,String status,String type) {
        return transactionRepo.findByFilters(userId,status,type);
    }

    @Override
    public List<Transaction> filterTransactionsByType(String userId, String type) {
        return filterTransactions(userId,null,type);
    }
}