package hu.ponte.imagevault.util;

import hu.ponte.imagevault.exception.EncryptionException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public final class EncryptionUtil {

    static String KEY_FILENAME = "secretKey";

    private EncryptionUtil() {
    }

    public static byte[] encrypt(byte[] original) {
        try {
            generateSecretKeyIfNeeded();
            var cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, readSecretKey());
            return cipher.doFinal(original);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt", e);
        }
    }

    public static byte[] decrypt(byte[] encrypted) {
        try {
            var cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, readSecretKey());
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new EncryptionException("Failed to decrypt", e);
        }
    }

    private static void generateSecretKeyIfNeeded() {
        if (Files.exists(Paths.get(KEY_FILENAME))) {
            return;
        }
        try (var fileOutputStream = new FileOutputStream(KEY_FILENAME)) {
            var keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // 256-bit AES key
            var secretKey = keyGen.generateKey();
            var encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            fileOutputStream.write(encodedKey.getBytes());
        } catch (Exception e) {
            throw new EncryptionException("Failed to generate secret key", e);
        }
    }

    private static SecretKey readSecretKey() {
        try {
            var keyBytes = Files.readAllBytes(Paths.get(KEY_FILENAME));
            var decodedKey = Base64.getDecoder().decode(keyBytes);
            return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        } catch (Exception e) {
            throw new EncryptionException("Failed to read secret key", e);
        }
    }

}
