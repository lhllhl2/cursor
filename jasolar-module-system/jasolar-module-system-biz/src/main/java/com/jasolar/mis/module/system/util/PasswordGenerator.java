package com.jasolar.mis.module.system.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple password utility retained for reuse across modules.
 */
public final class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private PasswordGenerator() {
    }

    public static String generateStrongPassword(int length) {
        int actualLength = Math.max(length, 12);
        SecureRandom random = new SecureRandom();
        List<Character> passwordChars = new ArrayList<>(actualLength);

        passwordChars.add(UPPER.charAt(random.nextInt(UPPER.length())));
        passwordChars.add(LOWER.charAt(random.nextInt(LOWER.length())));
        passwordChars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        passwordChars.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        String allChars = UPPER + LOWER + DIGITS + SPECIAL;
        for (int i = 4; i < actualLength; i++) {
            passwordChars.add(allChars.charAt(random.nextInt(allChars.length())));
        }

        Collections.shuffle(passwordChars, random);

        StringBuilder password = new StringBuilder(actualLength);
        for (Character c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }
}
