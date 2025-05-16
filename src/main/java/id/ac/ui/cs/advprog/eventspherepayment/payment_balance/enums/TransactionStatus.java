
package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums;

import lombok.Getter;

@Getter
public enum TransactionStatus {
    SUCCESS("SUCCESS"),
    FAILED("FAILED");

    private final String value;

    TransactionStatus(String value) {
        this.value = value;
    }

    public static boolean contains(String param) {
        for (TransactionStatus status : TransactionStatus.values()) {
            if (status.name().equals(param)) {
                return true;
            }
        }
        return false;
    }
}
