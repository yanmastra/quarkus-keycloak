package io.yanmastra.quarkusBase.utils;

import java.security.SecureRandom;

public class PasswordGenerator {
    private static final char[] SPECIAL_CHARACTERS = new char[]{'!', '@', '#', '$', '?', '%', '*', '&'};
    private static final char[] ALPHABET = new char[26];

    static {
        int i = 0;
        for (char ch = 'a'; ch <= 'z'; ch++) {
            ALPHABET[i++] = ch;
        }
    }

    // Lazy holder — SecureRandom is created at first use (runtime), not at build time
    private static class RandomHolder {
        static final SecureRandom INSTANCE = new SecureRandom();
    }

    private static SecureRandom random() {
        return RandomHolder.INSTANCE;
    }

    public static String generatePassword(int length, boolean includeSpecial) {
        SecureRandom rng = random();
        byte[] result = new byte[length];

        for (int i = 0; i < length; i++) {
            int act = rng.nextInt(4);
            char ch;
            switch (act) {
                case 0 -> ch = ALPHABET[rng.nextInt(26)];
                case 1 -> ch = includeSpecial
                        ? SPECIAL_CHARACTERS[rng.nextInt(SPECIAL_CHARACTERS.length)]
                        : ALPHABET[rng.nextInt(26)];
                case 2 -> ch = Character.toUpperCase(ALPHABET[rng.nextInt(26)]);
                default -> ch = (char) ('0' + rng.nextInt(9));
            }
            result[i] = (byte) ch;
        }

        return new String(result);
    }
}