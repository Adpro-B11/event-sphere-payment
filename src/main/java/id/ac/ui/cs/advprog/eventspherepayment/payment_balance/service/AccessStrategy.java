package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.service;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.Transaction;

import java.util.List;

public interface AccessStrategy {
    void createTransaction(Transaction transaction);
    void deleteTransaction(String transactionId);
    List<Transaction> viewAllTransactions();
    List<Transaction> viewUserTransactions(String userId);
    List<Transaction> filterTransactions(String userId,String status,String type);
    List<Transaction> filterTransactionsByType(String userId, String type);
}
