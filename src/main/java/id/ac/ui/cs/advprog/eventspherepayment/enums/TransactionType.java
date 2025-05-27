package id.ac.ui.cs.advprog.eventspherepayment.enums;

import lombok.Getter;

@Getter
public enum TransactionType {
    TOPUP_BALANCE("TOPUP_BALANCE"),
    TICKET_PURCHASE("TICKET_PURCHASE");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public static boolean contains(String param) {
        for (TransactionType type : TransactionType.values()) {
            if (type.name().equals(param)) {
                return true;
            }
        }
        return false;
    }
}
