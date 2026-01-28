package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Component
public class KeyValidator {

    private final IdFieldValidator idFieldValidator;
    private final TransactionTimestampContext transactionTimestampContext;
    private final DisplayFieldsValidator displayFieldsValidator;

    private final String keyNameValidationPattern;

    public KeyValidator(IdFieldValidator idFieldValidator,
                        TransactionTimestampContext transactionTimestampContext,
                        DisplayFieldsValidator displayFieldsValidator, @Value("${validation.key.name:}") String keyNameValidationPattern) {
        this.idFieldValidator = idFieldValidator;
        this.transactionTimestampContext = transactionTimestampContext;
        this.displayFieldsValidator = displayFieldsValidator;
        this.keyNameValidationPattern = keyNameValidationPattern;
    }

    public void validateCreation(Key key) {
        validateKeyName(key);
        validateProject(key);
        validateAllowedIpAddressRanges(key);
        displayFieldsValidator.validateDisplayName(key.getDisplayName(), "Key", key.getName());
        long now = transactionTimestampContext.getTimestamp();
        Long expiresAt = key.getExpiresAt();
        if (expiresAt != null && expiresAt <= now) {
            throw new IllegalArgumentException("Key expiresAt ms: '" + expiresAt + "' should be greater than current epoch ms: '" + now + "'");
        }
    }

    private void validateKeyName(Key key) {
        final String keyName = key.getName();

        idFieldValidator.validateName("Key", keyName);

        if (StringUtils.isEmpty(keyNameValidationPattern)) {
            log.debug("Key name validation pattern is empty, skipping validation for key: {}", keyName);
            return;
        }

        if (!Pattern.matches(keyNameValidationPattern, keyName)) {
            throw new IllegalArgumentException("Key name '" + keyName
                    + "' does not match the required pattern: " + keyNameValidationPattern);
        }
    }

    public void validateUpdate(String keyName, Key key, KeyEntity existingEntity) {
        if (!Objects.equals(keyName, key.getName())) {
            throw new IllegalArgumentException("Key with name: '" + keyName + "' can not be renamed. New key name: '" + key.getName() + "'");
        }
        validateProject(key);
        validateAllowedIpAddressRanges(key);
        displayFieldsValidator.validateDisplayName(key.getDisplayName(), "Key", key.getName());
        Long expiresAt = key.getExpiresAt();
        long createdAt = existingEntity.getCreatedAt();
        if (expiresAt != null && expiresAt <= createdAt) {
            throw new IllegalArgumentException("Key expiresAt ms: '" + expiresAt + "' should be greater than key createdAt ms: '" + createdAt + "'");
        }
    }

    private void validateProject(Key key) {
        if (StringUtils.isBlank(key.getProject())) {
            throw new IllegalArgumentException("Project is required. Key name: " + key.getName());
        }
    }

    private void validateAllowedIpAddressRanges(Key key) {
        if (CollectionUtils.isEmpty(key.getAllowedIpAddressRanges())) {
            log.debug("Key allowed IpAddress ranges is empty, skipping validation for key: {}", key.getKey());
            return;
        }
        var errors = key.getAllowedIpAddressRanges().stream()
                .map(cidr -> {
                    try {
                        validateCidr(cidr);
                        return null;
                    } catch (IllegalArgumentException ex) {
                        return ex.getMessage();
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(". ", errors));
        }
    }

    private void validateCidr(String cidr) {
        String[] parts = cidr.trim().split("/");
        final String invalidCidr = "Invalid CIDR: " + cidr + ".";
        if (parts.length != 2) {
            throw new IllegalArgumentException(invalidCidr);
        }

        String base = parts[0].trim();
        int prefixLen = Integer.parseInt(parts[1].trim());
        InetAddress baseAddr;
        try {
            baseAddr = InetAddress.getByName(base);
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException(invalidCidr + "No such host is known (" + base + ")");
        }
        byte[] baseBytes = baseAddr.getAddress();

        int maxPrefix = baseBytes.length * 8; // 32 for IPv4, 128 for IPv6
        if (prefixLen < 0 || prefixLen > maxPrefix) {
            throw new IllegalArgumentException(invalidCidr + "Invalid prefix length " + prefixLen
                    + " for " + maxPrefix + "-bit address.");
        }
    }
}