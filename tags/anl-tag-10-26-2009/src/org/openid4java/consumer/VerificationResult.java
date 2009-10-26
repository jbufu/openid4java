/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.consumer;

import org.openid4java.discovery.Identifier;
import org.openid4java.message.MessageException;
import org.openid4java.message.Message;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class VerificationResult
{
    /**
     * Identifier on which authentication and verification were performed
     * succesfully and which can be used henceforth by Relying Parties to
     * identify the user.
     * <p>
     * Null if authentication or verification on the claimed identifier failed.
     */
    private Identifier _verifiedId;

    /**
     * Optional parameter returned in a failure response to a immediate
     * authentication request (AuthImmediateFailure).
     */
    private String _opSetupUrl;


    /**
     * The authentication response received from the server.
     */
    private Message _authResponse;

    /**
     * An unstructured status / error message.
     */
    private String _statusMsg;

    /**
     * Gets the verified identifier.
     */
    public Identifier getVerifiedId()
    {
        return _verifiedId;
    }

    /**
     * Sets the verified identifier.
     */
    public void setVerifiedId(Identifier verifiedId)
    {
        this._verifiedId = verifiedId;
    }

    /**
     * Gets the optional OP user_setup_url parameter, if one was returned in a
     * failure response to a immediate authentication request.
     * <p>
     * Null if the response did not include the user_setup_url parameter.
     */
    public String getOPSetupUrl()
    {
        return _opSetupUrl;
    }

    /**
     * Sets the OP user_setup_url parameter, if one was returned in a failure
     * response to a immediate authentication request.
     */
    public void setOPSetupUrl(String opSetupUrl) throws MessageException
    {
        this._opSetupUrl = opSetupUrl;
    }

    /**
     * Gets the authentication response message received from the server.
     */
    public Message getAuthResponse()
    {
        return _authResponse;
    }

    /**
     * Sets the authentication response message received from the server.
     */
    public void setAuthResponse(Message authResponse)
    {
        this._authResponse = authResponse;
    }

    public String getStatusMsg()
    {
        return _statusMsg;
    }

    public void setStatusMsg(String statusMsg)
    {
        this._statusMsg = statusMsg;
    }
}
