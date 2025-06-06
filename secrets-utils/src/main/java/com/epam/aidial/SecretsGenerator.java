package com.epam.aidial;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

@Slf4j
class SecretsGenerator {
    private static final int AES_KEY_SIZE = 192;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final String AES = "AES";
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";


    private static byte[] extractBody(byte[] decodedKey) {
        return Arrays.copyOfRange(decodedKey, 12, decodedKey.length);
    }

    private static byte[] extractIv(byte[] decodedKey) {
        return Arrays.copyOfRange(decodedKey, 0, 12);
    }

    private static GCMParameterSpec getGcmParameters(byte[] iv) {
        return new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
    }

    private static byte[] getIv() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }

    private static SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        final var keyGenerator = KeyGenerator.getInstance(AES);
        keyGenerator.init(AES_KEY_SIZE);

        return keyGenerator.generateKey();
    }

    @SneakyThrows
    public static void generateMasterKeyAndEncryptedDataKey() {
        final var cipher = Cipher.getInstance(AES_GCM_NO_PADDING);

        final var dbPassword = generateSecretKey();
        final var dbPasswordBytes = dbPassword.getEncoded();
        final var dbPasswordKey = Base64.getEncoder().encodeToString(dbPasswordBytes);
        log.info(">>> db password size: {}", dbPasswordBytes.length * 8);
        log.info("============== base64 db password: {}", dbPasswordKey);

        final var masterKey = generateSecretKey();
        final var masterKeyBytes = masterKey.getEncoded();
        final var base64MasterKey = Base64.getEncoder().encodeToString(masterKeyBytes);
        log.info(">>> master key size: {}", masterKeyBytes.length * 8);
        log.info("============== base64 master key: {}", base64MasterKey);
        final var key = generateSecretKey();
        final var keyBytes = key.getEncoded();
        final var base64EncryptKey = Base64.getEncoder().encodeToString(keyBytes);
        log.info(">>> encryption key size: {}", keyBytes.length * 8);
        log.info(">>> base64 encryption key:{}", base64EncryptKey);

        final var iv = getIv();
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, getGcmParameters(iv));
        final var encrypted = ArrayUtils.addAll(iv, cipher.doFinal(keyBytes));
        final var base64EncryptedEncryptKey = Base64.getEncoder().encodeToString(encrypted);
        log.info("============== base64 encrypted encryption key: {}", base64EncryptedEncryptKey);

        //decrypt for example
        final var decodedKey = Base64.getDecoder().decode(base64EncryptedEncryptKey);
        final var ivExtracted = extractIv(decodedKey);
        cipher.init(Cipher.DECRYPT_MODE, masterKey, getGcmParameters(ivExtracted));
        final var decrypted = cipher.doFinal(extractBody(decodedKey));
        final var base64DecryptedEncryptKey = Base64.getEncoder().encodeToString(decrypted);
        log.info(">>> base64 decrypted encryption key: {}", base64DecryptedEncryptKey);

        log.info("base64DecryptedEncryptKey is equals base64EncryptKey {}", Objects.equals(base64DecryptedEncryptKey, base64EncryptKey));

    }
}
