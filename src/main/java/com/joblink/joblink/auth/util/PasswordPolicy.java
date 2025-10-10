package com.joblink.joblink.auth.util;



import java.util.regex.Pattern;

public class PasswordPolicy {
    private static final Pattern PWD =
            Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    public static boolean isValid(String raw) {
        return raw != null && PWD.matcher(raw).matches();
    }
}
