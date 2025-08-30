package com.sporty.util;


import java.math.*;

/**
 * As of not used, but that can be used later to covert the currency. We can always store the currency in EURO,
 * Services can accept the currency code and convert it into EURO while saving and while returning convert to user currency
 */
public final class MoneyUtil {
    private MoneyUtil() {
    }

    public static long euroToCents(String amountEuros) {
        BigDecimal bd = new BigDecimal(amountEuros).movePointRight(2);
        return bd.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    public static String centsToEuroStr(long cents) {
        BigDecimal bd = new BigDecimal(cents).movePointLeft(2);
        return bd.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}