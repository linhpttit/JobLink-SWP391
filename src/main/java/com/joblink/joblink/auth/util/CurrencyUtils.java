package com.joblink.joblink.auth.util;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    public static String formatVND(double amount) {
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyVN = NumberFormat.getCurrencyInstance(localeVN);
        String formatted = currencyVN.format(amount);
        // Mặc định là "1.200.000 ₫", ta đổi ký hiệu ₫ thành VND
        return formatted.replace("₫", "VND").trim();
    }
}
