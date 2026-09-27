package com.netk.mvolacash.admin.activation;

/** Result returned by an activation signer. */
public final class ActivationResponse {
    private final String activationCode;

    public ActivationResponse(String activationCode) {
        this.activationCode = activationCode;
    }

    public String getActivationCode() {
        return activationCode;
    }
}

