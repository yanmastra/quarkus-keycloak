package io.yanmastra.authentication.utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    private ValidationUtils(){}
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_! #$%&'*+/=?`{|}~^. -]+@[a-zA-Z0-9. -]+$");

    public static boolean isEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
