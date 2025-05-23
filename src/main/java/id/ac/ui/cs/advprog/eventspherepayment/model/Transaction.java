package id.ac.ui.cs.advprog.eventspherepayment.model;

import id.ac.ui.cs.advprog.eventspherepayment.enums.TransactionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import jakarta.persistence.*;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "transaction_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Transaction {

    @Id
    @Column(name = "transaction_id", columnDefinition = "BINARY(16)")
    private UUID transactionId;

    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "status")
    private String status;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Transaction(UUID transactionId,
                          UUID userId,
                          String type,
                          double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be positive");
        }

        this.transactionId = transactionId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void setStatus(String status) {
        if (!TransactionStatus.contains(status)) {
            throw new IllegalArgumentException("Invalid transaction status: " + status);
        }
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public abstract void validateTransaction(String method, Map<String, String> paymentData);
}