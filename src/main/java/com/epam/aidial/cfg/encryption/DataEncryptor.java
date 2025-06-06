package com.epam.aidial.cfg.encryption;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Arrays;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@RequiredArgsConstructor
@Slf4j
public class DataEncryptor {
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final String AES = "AES";
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";

    @SneakyThrows
    public static byte[] decryptedKey(String masterKey, String encryptedEncryptionKey) {
        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);

        final byte[] masterKeyBytes = Base64.getDecoder().decode(masterKey);
        final SecretKeySpec secretKeySpec = new SecretKeySpec(masterKeyBytes, AES);

        final byte[] decodedKey = Base64.getDecoder().decode(encryptedEncryptionKey);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, getGcmParameters(extractIv(decodedKey)));
        return cipher.doFinal(extractBody(decodedKey));
    }

    public static String decryptedKeyBase64(String masterKey, String encryptedEncryptionKey) {
        return new String(Base64.getEncoder().encode(decryptedKey(masterKey, encryptedEncryptionKey)));
    }

    @NotNull
    private static byte[] extractBody(byte[] data) {
        return Arrays.copyOfRange(data, GCM_IV_LENGTH, data.length);
    }

    @NotNull
    private static byte[] extractIv(byte[] data) {
        return Arrays.copyOfRange(data, 0, GCM_IV_LENGTH);
    }

    @NotNull
    private static GCMParameterSpec getGcmParameters(byte[] iv) {
        return new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, iv);
    }

}