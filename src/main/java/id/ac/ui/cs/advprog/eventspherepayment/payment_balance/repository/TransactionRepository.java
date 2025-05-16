package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.repository;

import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.factory.TransactionFactory;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.factory.TransactionFactoryProducer;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.Transaction;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.TicketPurchaseTransaction;
import id.ac.ui.cs.advprog.eventspherepayment.payment_balance.model.TopUpTransaction;
import org.hibernate.validator.constraints.pl.REGON;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TransactionRepository {
    private final Map<String, Transaction> transactionStorage = new HashMap<>();
    private final TransactionFactoryProducer factoryProducer;

    public TransactionRepository(TransactionFactoryProducer factoryProducer) {
        this.factoryProducer = Objects.requireNonNull(factoryProducer);
    }

    public Transaction createAndSave(String type, String transactionId, String userId,
                                     double amount, String method, Map<String, String> data) {
        TransactionFactory factory = factoryProducer.getFactory(type, transactionId, userId, amount, method, data);
        Transaction transaction = factory.createTransaction();
        return save(transaction);
    }

    public Transaction save(Transaction transaction) {
        Objects.requireNonNull(transaction);
        Objects.requireNonNull(transaction.getTransactionId());
        transaction.validateTransaction();
        transactionStorage.put(transaction.getTransactionId(), transaction);
        return transaction;
    }

    public Optional<Transaction> findById(String id) {
        return Optional.ofNullable(transactionStorage.get(id));
    }

    public List<Transaction> findAll() {
        return new ArrayList<>(transactionStorage.values());
    }

    public List<Transaction> findByFilters(String userId, String status, String type) {
        return transactionStorage.values().stream()
                .filter(trx -> userId == null || userId.equals(trx.getUserId()))
                .filter(trx -> status == null || status.equalsIgnoreCase(trx.getStatus()))
                .filter(trx -> type == null || type.equalsIgnoreCase(trx.getType()))
                .toList();
    }

    public void deleteById(String id) {
        transactionStorage.remove(id);
    }
}