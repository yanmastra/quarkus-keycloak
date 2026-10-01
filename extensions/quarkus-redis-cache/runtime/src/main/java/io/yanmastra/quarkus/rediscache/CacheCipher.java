package io.yanmastra.quarkus.rediscache;

import org.jboss.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/** AES-256-GCM for cache values. Output is {@code base64(iv || ciphertext)} so it is safe as a Redis string. */
public final class CacheCipher {
    private static final Logger logger = Logger.getLogger(CacheCipher.class.getName());

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    // Lazy holder — SecureRandom initialized at runtime, not at GraalVM build time
    private static class RandomHolder {
        static final SecureRandom INSTANCE = new SecureRandom();
    }

    private final SecretKey key;

    public CacheCipher(String base64Key) {
        this.key = new SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES");
    }

    public static String generateBase64Key() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256, RandomHolder.INSTANCE);
            return Base64.getEncoder().encodeToString(generator.generateKey().getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate a cache encryption key: " + e.getMessage(), e);
        }
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            RandomHolder.INSTANCE.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] output = new byte[GCM_IV_LENGTH + cipherText.length];
            System.arraycopy(iv, 0, output, 0, GCM_IV_LENGTH);
            System.arraycopy(cipherText, 0, output, GCM_IV_LENGTH, cipherText.length);
            return Base64.getEncoder().encodeToString(output);
        } catch (Exception e) {
            throw new IllegalStateException("Could not encrypt cache value: " + e.getMessage(), e);
        }
    }

    /** @return the plain text, or {@code null} when the value cannot be decrypted with this key. */
    public String decrypt(String stored) {
        try {
            byte[] bytes = Base64.getDecoder().decode(stored);
            if (bytes.length <= GCM_IV_LENGTH) return null;

            byte[] iv = Arrays.copyOfRange(bytes, 0, GCM_IV_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(bytes, GCM_IV_LENGTH, bytes.length);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("Could not decrypt a Redis cache value, treating it as missing: " + e.getMessage());
            return null;
        }
    }
}
