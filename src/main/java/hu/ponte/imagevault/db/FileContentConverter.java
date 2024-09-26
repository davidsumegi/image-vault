package hu.ponte.imagevault.db;

import hu.ponte.imagevault.util.EncryptionUtil;
import jakarta.persistence.AttributeConverter;

public class FileContentConverter implements AttributeConverter<byte[], byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(byte[] bytes) {
        return EncryptionUtil.encrypt(bytes);
    }

    @Override
    public byte[] convertToEntityAttribute(byte[] bytes) {
        return EncryptionUtil.decrypt(bytes);
    }
}
