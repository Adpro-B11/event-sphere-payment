package id.ac.ui.cs.advprog.eventspherepayment.repository;

import id.ac.ui.cs.advprog.eventspherepayment.factory.TransactionFactoryProducer;
import id.ac.ui.cs.advprog.eventspherepayment.model.TicketPurchaseTransaction;
import id.ac.ui.cs.advprog.eventspherepayment.model.TopUpTransaction;
import id.ac.ui.cs.advprog.eventspherepayment.model.Transaction;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class TransactionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Transaction createAndSave(String type,
                                     String transactionId,
                                     String userId,
                                     double amount,
                                     String method,
                                     Map<String, String> data) {

        UUID txUuid   = UUID.fromString(transactionId);
        UUID userUuid = UUID.fromString(userId);

        Transaction tx = TransactionFactoryProducer
                .getFactory(type, txUuid, userUuid, amount, method, data)
                .createTransaction();

        return save(tx);
    }

    public Transaction save(Transaction tx) {
        Objects.requireNonNull(tx, "Transaction must not be null");
        entityManager.persist(tx);
        entityManager.flush();
        return tx;
    }

    public Transaction update(Transaction tx) {
        Objects.requireNonNull(tx, "Transaction must not be null");
        Transaction managed = entityManager.merge(tx);
        entityManager.flush();
        return managed;
    }

    public Optional<Transaction> findById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return Optional.ofNullable(entityManager.find(Transaction.class, uuid));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public List<Transaction> findAll() {
        return entityManager.createQuery(
                "SELECT t FROM Transaction t ORDER BY t.createdAt DESC", Transaction.class
        ).getResultList();
    }

    public List<Transaction> findByFilters(
            String userId,
            String status,
            String type,
            String method,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Transaction> cq = cb.createQuery(Transaction.class);
        Root<Transaction> root = cq.from(Transaction.class);

        List<Predicate> predicates = new ArrayList<>();

        if (userId != null) {
            try {
                predicates.add(cb.equal(root.get("userId"), UUID.fromString(userId)));
            } catch (IllegalArgumentException ex) {
                return Collections.emptyList(); // UUID tak valid
            }
        }
        if (status != null)
            predicates.add(cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
        if (type != null)
            predicates.add(cb.equal(cb.lower(root.get("type")), type.toLowerCase()));
        if (createdAfter != null)
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter));
        if (createdBefore != null)
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdBefore));

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("createdAt")));

        // hasil awal
        List<Transaction> result = entityManager.createQuery(cq).getResultList();

        if (method != null) {
            result = result.stream().filter(tx -> {
                if (tx instanceof TopUpTransaction tut)
                    return method.equalsIgnoreCase(tut.getMethod());
                if (tx instanceof TicketPurchaseTransaction tpt)
                    return method.equalsIgnoreCase(tpt.getMethod());
                return false;
            }).collect(Collectors.toList());
        }
        return result;
    }

    public void deleteById(String id) {
        findById(id).ifPresent(tx -> {
            entityManager.remove(tx);
            entityManager.flush();
        });
    }
}
