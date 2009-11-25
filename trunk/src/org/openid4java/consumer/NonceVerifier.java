/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.consumer;

import com.google.inject.ImplementedBy;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
@ImplementedBy(InMemoryNonceVerifier.class)
public interface NonceVerifier
{
    /**
     * This noce is valid and it was not seen before. Nonce should be accepted.
     */
    public static final int OK = 0;

    /**
     * The nonce was seen before. Nonce should be rejected.
     */
    public static final int SEEN = 1;

    /**
     * The timestamp of the nonce is invalid, it cannot be parsed. Nonce should be rejected.
     */
    public static final int INVALID_TIMESTAMP = 2;

    /**
     * The timestamp of the nonce is too old and it is not tracked anymore. Nonce should be rejected.
     */
    public static final int TOO_OLD = 3;

    /**
     * Checks if a nonce was seen before. It also checks if the time stamp at the beginning of the noce is valid.
     * Also, if old nonces are discarded the it should check if the time stamp for this noce is still valid.
     *
     * @return {@link #OK} only if this nonce has a valid time stamp, the time stamp did not age and the nonce was not
     * seen before.
     */
    public int seen(String opUrl, String nonce);

    /**
     * Returns the expiration timeout for nonces, in seconds.
     */
    public int getMaxAge();

    /**
     * Sets the expiration timeout for nonces, in seconds.
     */
    public void setMaxAge(int ageSeconds);
}
