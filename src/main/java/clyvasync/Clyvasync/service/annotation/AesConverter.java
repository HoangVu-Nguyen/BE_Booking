package clyvasync.Clyvasync.service.annotation;

import org.jasypt.util.text.AES256TextEncryptor;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AesConverter implements AttributeConverter<String, String> {

    private static final String SECRET_KEY = System.getenv("DB_ENCRYPTION_KEY");
    private final AES256TextEncryptor encryptor;

    public AesConverter() {
        encryptor = new AES256TextEncryptor();
        encryptor.setPassword(SECRET_KEY);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return encryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return encryptor.decrypt(dbData);
    }
}