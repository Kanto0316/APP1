package com.netk.mvolacash.admin.activation;

import java.util.Locale;

/** A normalized device activation request, ready for a future format validator. */
public final class ActivationRequest {
    private final String requestCode;

    public ActivationRequest(String requestCode) {
        this.requestCode = normalize(requestCode);
    }

    public String getRequestCode() {
        return requestCode;
    }

    public boolean isEmpty() {
        return requestCode.isEmpty();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}

