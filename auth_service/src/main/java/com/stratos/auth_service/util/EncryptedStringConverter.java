package com.stratos.auth_service.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

// GitHub tokens are sent back to GitHub, so they are encrypted rather than hashed.
// Each stored value is base64(IV + ciphertext), with a fresh IV per write.
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int AUTH_TAG_BITS = 128;

    private final SecretKey key;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptedStringConverter(@Value("${github.token-encryption-key}") String base64Key) {
        this.key = new SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES");
    }

    @Override
    public String convertToDatabaseColumn(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(AUTH_TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] ivAndCipherText = ByteBuffer.allocate(iv.length + cipherText.length)
                    .put(iv)
                    .put(cipherText)
                    .array();
            return Base64.getEncoder().encodeToString(ivAndCipherText);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not encrypt value", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String storedValue) {
        if (storedValue == null) {
            return null;
        }
        try {
            byte[] ivAndCipherText = Base64.getDecoder().decode(storedValue);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(AUTH_TAG_BITS, ivAndCipherText, 0, IV_BYTES));
            byte[] plainText = cipher.doFinal(ivAndCipherText, IV_BYTES, ivAndCipherText.length - IV_BYTES);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Could not decrypt value", e);
        }
    }
}
