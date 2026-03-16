package com.epam.aidial.cfg.domain.validator;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class CidrValidator {
    public void validate(String cidr) {
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
