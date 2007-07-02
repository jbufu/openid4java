/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.message.ax;

import org.openid4java.message.ParameterList;
import org.openid4java.message.MessageException;
import org.openid4java.message.Parameter;

import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Implements the extension for Attribute Exchange store responses.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class StoreResponse extends AxMessage
{
    private static Logger _log = Logger.getLogger(StoreResponse.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /**
     * Constructs a Store Response with an empty parameter list.
     */
    protected StoreResponse()
    {
        _parameters.set(new Parameter("mode", "store_response"));

        if (DEBUG) _log.debug("Created empty store request.");
    }

    /**
     * Constructs a Store Response with an empty parameter list.
     */
    public static StoreResponse createStoreResponse()
    {
        return new StoreResponse();
    }

    /**
     * Constructs a StoreResponse from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    protected StoreResponse(ParameterList params)
    {
        super(params);
    }

    /**
     * Constructs a StoreResponse from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    public static StoreResponse createStoreResponse(ParameterList params)
            throws MessageException
    {
        StoreResponse resp = new StoreResponse(params);

        if (! resp.isValid())
            throw new MessageException("Invalid parameters for a store response");

        if (DEBUG)
            _log.debug("Created store response from parameter list:\n" + params);

        return resp;
    }

    /**
     * Marks the Store Response as a failure, by setting the appropirate
     * parameters.
     *
     * @param       description     Describes the error condition leading to
     *                              the failure response
     */
    protected void setFailure(String description)
    {
        _parameters.set(new Parameter("status", "failure"));

        if (description != null)
            _parameters.set(new Parameter("status.description", description));
    }

    /**
     * Returns true if the Store Response is a failure message, true if it is
     * a success response.
     */
    public boolean hasFailed()
    {
        return _parameters.hasParameter("status")  &&
                "failure".equals(_parameters.getParameterValue("status"));
    }

    /**
     * Gets the status of the Store Response if the 'status' parameter is part
     * of the response, or null otherwise.
     */
    public String getStatus()
    {
        return _parameters.hasParameter("status")  ?
                _parameters.getParameterValue("status") : null;
    }

    /**
     * Gets the status of the Store Response if the 'status.description'
     * parameter is part of the response, or null otherwise.
     */
    public String getStatusDescription()
    {
        return _parameters.hasParameter("status.description") ?
                _parameters.getParameterValue("status.description") : null;
    }

    /**
     * Checks the validity of the extension.
     * <p>
     * Used when constructing a extension from a parameter list.
     *
     * @return      True if the extension is valid, false otherwise.
     */
    private boolean isValid()
    {
        if ( getStatusDescription() != null && getStatus() == null)
            return false;

        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            Parameter param = (Parameter) it.next();
            String paramName = param.getKey();

            if (! paramName.equals("mode") &&
                    ! paramName.equals("status") &&
                    ! paramName.equals("status.description"))
            {
                _log.warn("Invalid parameter name in store response: " + paramName);
                return false;
            }

            if ( paramName.equals("status") &&
                    ! "failure".equals(param.getValue()) )
            {
                _log.warn("Invalid status value: " + param.getValue());
                return false;
            }
        }

        return true;
    }

}
