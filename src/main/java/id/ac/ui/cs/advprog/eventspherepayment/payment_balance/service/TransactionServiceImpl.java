package id.ac.ui.cs.advprog.eventsphere.payment_balance.service;

import id.ac.ui.cs.advprog.eventsphere.auth.service.UserService;
import id.ac.ui.cs.advprog.eventsphere.payment_balance.enums.TransactionStatus;
import id.ac.ui.cs.advprog.eventsphere.payment_balance.enums.TransactionType;
import id.ac.ui.cs.advprog.eventsphere.payment_balance.model.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TransactionServiceImpl implements TransactionService {
    private AccessStrategy strategy;
    private final UserService userBalanceService;

    public TransactionServiceImpl(UserService userBalanceService) {
        this.userBalanceService = userBalanceService;
    }

    @Override
    public void setStrategy(AccessStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void createTransaction(String type, String transactionId, String userId,
                                  double amount, String method, Map<String, String> data) {
        // Validate if user has sufficient balance for ticket purchase
        if (type.equals(TransactionType.TICKET_PURCHASE.name())) {
            boolean hasSufficientBalance = userBalanceService.deductBalance(userId, amount);
            if (!hasSufficientBalance) {
                throw new IllegalStateException("Insufficient balance for ticket purchase");
            }
        }

        if (strategy instanceof UserAccessStrategy userStrategy) {
            Transaction transaction = userStrategy.createAndProcessTransaction(type, transactionId, userId, amount, method, data);

            // Process balance updates based on transaction result
            if (transaction.getStatus().equals(TransactionStatus.SUCCESS.name())) {
                if (type.equals(TransactionType.TOPUP_BALANCE.name())) {
                    userBalanceService.addBalance(userId, amount);
                }
            } else {
                // Refund if transaction failed for ticket purchase
                if (type.equals(TransactionType.TICKET_PURCHASE.name())) {
                    userBalanceService.addBalance(userId, amount);
                }
            }
        } else {
            throw new UnsupportedOperationException("Current strategy does not support this operation");
        }
    }

    @Override
    public void deleteTransaction(String transactionId) {
        strategy.deleteTransaction(transactionId);
    }

    @Override
    public List<Transaction> viewAllTransactions() {
        return strategy.viewAllTransactions();
    }

    @Override
    public List<Transaction> viewUserTransactions(String userId) {
        return strategy.viewUserTransactions(userId);
    }

    @Override
    public List<Transaction> filterTransactions(String userId, String status, String type) {
        return strategy.filterTransactions(userId, status, type);
    }

    @Override
    public List<Transaction> filterTransactionsByType(String userId, String type) {
        return strategy.filterTransactionsByType(userId, type);
    }
}