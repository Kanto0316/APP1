package com.netk.mvolacash.admin.activation;

/** Contract for the secure signer that will be implemented in a later phase. */
public interface ActivationSigner {
    ActivationResponse sign(ActivationRequest request);
}

