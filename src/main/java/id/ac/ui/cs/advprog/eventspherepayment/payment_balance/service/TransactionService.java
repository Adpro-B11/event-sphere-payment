package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.service;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.Transaction;

import java.util.List;
import java.util.Map;

public interface TransactionService {
    void setStrategy(AccessStrategy strategy);
    void createTransaction(String type, String transactionId, String userId,
                           double amount, String method, Map<String, String> data);
    void deleteTransaction(String transactionId);
    List<Transaction> viewAllTransactions();
    List<Transaction> viewUserTransactions(String userId);
    List<Transaction> filterTransactions(String userId, String status, String type);
    List<Transaction> filterTransactionsByType(String userId, String type);
}