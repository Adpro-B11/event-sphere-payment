package id.ac.ui.cs.advprog.eventspherepayment.payment_balance.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    BANK_TRANSFER("BANK_TRANSFER"),
    CREDIT_CARD("CREDIT_CARD");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    public static boolean contains(String param) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.name().equals(param)) {
                return true;
            }
        }
        return false;
    }
}
