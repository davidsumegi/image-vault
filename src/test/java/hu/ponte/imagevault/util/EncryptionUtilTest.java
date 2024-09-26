package hu.ponte.imagevault.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptionUtilTest {

    @Test
    @SneakyThrows
    void testEncryption() {
        var original = "test".getBytes();
        Files.deleteIfExists(Paths.get(EncryptionUtil.KEY_FILENAME));
        // First encryption (no secretKey file)
        var encrypted = EncryptionUtil.encrypt(original);
        // The secretKey file has been created
        assertThat(Files.exists(Paths.get(EncryptionUtil.KEY_FILENAME))).isTrue();
        assertThat(encrypted).isNotEqualTo(original);
        // Decryption
        assertThat(EncryptionUtil.decrypt(encrypted)).isEqualTo(original);
        // Second encryption (secretKey file is reused)
        assertThat(EncryptionUtil.encrypt(original)).isEqualTo(encrypted);
    }
}
