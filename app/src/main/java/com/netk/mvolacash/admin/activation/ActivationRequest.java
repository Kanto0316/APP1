package com.netk.mvolacash.admin.activation;

import java.util.Locale;

/**
 * A Client installation request.
 *
 * <p>The wire format is 32 characters from the unambiguous Crockford subset
 * {@code 23456789ABCDEFGHJKMNPQRSTUVWXYZ}, displayed as eight groups of four.
 * Hyphens and ASCII whitespace are presentation separators and are ignored.</p>
 */
public final class ActivationRequest {
    public static final int ID_LENGTH = 32;
    public static final String ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";

    private final String installationId;

    public ActivationRequest(String requestCode) {
        this.installationId = normalize(requestCode);
    }

    /** Returns the normalized installation identity without visual separators. */
    public String getInstallationId() {
        return installationId;
    }

    /** Returns the normalized, human-readable request code. */
    public String getRequestCode() {
        if (!isValid()) return installationId;
        StringBuilder result = new StringBuilder(39);
        for (int i = 0; i < installationId.length(); i++) {
            if (i > 0 && i % 4 == 0) result.append('-');
            result.append(installationId.charAt(i));
        }
        return result.toString();
    }

    public boolean isEmpty() { return installationId.isEmpty(); }

    public boolean isValid() {
        if (installationId.length() != ID_LENGTH) return false;
        for (int i = 0; i < installationId.length(); i++) {
            if (ALPHABET.indexOf(installationId.charAt(i)) < 0) return false;
        }
        return true;
    }

    private static String normalize(String value) {
        if (value == null) return "";
        String upper = value.trim().toUpperCase(Locale.ROOT);
        StringBuilder normalized = new StringBuilder(upper.length());
        for (int i = 0; i < upper.length(); i++) {
            char c = upper.charAt(i);
            if (c != '-' && c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                normalized.append(c);
            }
        }
        return normalized.toString();
    }
}
