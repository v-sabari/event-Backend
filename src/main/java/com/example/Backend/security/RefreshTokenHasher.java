package com.example.Backend.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Turns a refresh-token JWT into a SHA-256 hex digest. Only the digest is ever
 * persisted, never the raw token: a compromise of the DB then yields nothing
 * usable, and lookups still work because a presented token is hashed with the
 * same function before being compared.
 */
public final class RefreshTokenHasher {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private RefreshTokenHasher() {
    }

    public static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            char[] out = new char[bytes.length * 2];
            for (int i = 0; i < bytes.length; i++) {
                int v = bytes[i] & 0xFF;
                out[i * 2] = HEX[v >>> 4];
                out[i * 2 + 1] = HEX[v & 0x0F];
            }
            return new String(out);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}