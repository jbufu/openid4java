/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message.ax;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.message.MessageException;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implements the extension for Attribute Exchange fetch responses.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class FetchResponse extends AxPayload
{
    private static Log _log = LogFactory.getLog(FetchResponse.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /**
     * Constructs a Fetch Response with an empty parameter list.
     */
    protected FetchResponse()
    {
        _parameters.set(new Parameter("mode", "fetch_response"));

        if (DEBUG) _log.debug("Created empty fetch response.");
    }

    /**
     * Constructs a Fetch Response with an empty parameter list.
     */
    public static FetchResponse createFetchResponse()
    {
        return new FetchResponse();
    }

    /**
     * Constructs a FetchResponse from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    protected FetchResponse(ParameterList params)
    {
        _parameters = params;
    }

    public static FetchResponse createFetchResponse(ParameterList params)
            throws MessageException
    {
        FetchResponse resp = new FetchResponse(params);

        if (! resp.isValid())
            throw new MessageException("Invalid parameters for a fetch response");

        if (DEBUG)
            _log.debug("Created fetch response from parameter list:\n" + params);

        return resp;
    }

    /**
     * Creates a FetchResponse from a FetchRequest message and the data released
     * by the user.
     *
     * @param req               FetchRequest message.
     * @param userData          The userData may be a Map<String alias, String value>
     *                          or a Map<String alias, List<String> values>. The attribute values
     *                          are provided by the calling application. If a list of values is
     *                          specified per attribute, at most n will be sent, where n is the
     *                          number of attribute values requested in the FetchRequest.
     * @return                  Properly formed FetchResponse.
     */
    public static FetchResponse createFetchResponse(FetchRequest req, Map userData)
        throws MessageException
    {
        FetchResponse resp = new FetchResponse();

        // go through each requested attribute
        Map attributes = req.getAttributes();

        for (Iterator i = attributes.keySet().iterator(); i.hasNext(); )
        {
            String alias = (String) i.next();

            // find attribute in userData
            Object value = userData.get(alias);

            // if the value isn't there, skip over it
            if (value == null)
            {
                continue;
            }

            // if the value is a string, add the single attribute to the response
            if (value instanceof String)
            {
                resp.addAttribute(alias, (String) attributes.get(alias), (String)value);
            }

            // if the value is a list (of string) iteratively add each attribute to the response
            else if (value instanceof List)
            {
                Iterator values = ((List)value).iterator();

                // only send up the the maximum requested number
                int max = req.getCount(alias);
                if (max == 0)
                    max = ((List)value).size();
                int count;
                for (count = 0; count < max && values.hasNext(); count++)
                {
                    // don't add null values to the response
                    String val = (String)values.next();
                    if (val == null)
                    {
                        count--; // disregard this as a value as we are skipping over it
                        continue;
                    }
                    resp.addAttribute(alias, (String) attributes.get(alias), val);
                }
            }
        }

        return resp;
    }

    /**
     * Sets the optional 'update_url' parameter where the OP can later re-post
     * fetch-response updates for the values of the requested attributes.
     *
     * @param       updateUrl   The URL where the RP accepts later updates
     *                          for the requested attributes.
     */
    public void setUpdateUrl(String updateUrl) throws MessageException
    {
        try
        {
            new URL(updateUrl);
        }
        catch (MalformedURLException e)
        {
            throw new MessageException("Invalid update_url: " + updateUrl);
        }

        if (DEBUG) _log.debug("Setting fetch response update_url: " + updateUrl);

        _parameters.set(new Parameter("update_url", updateUrl));
    }

    /**
     * Gets the optional 'update_url' parameter if available, or null otherwise.
     */
    public String getUpdateUrl()
    {
        return _parameters.hasParameter("update_url") ?
                _parameters.getParameterValue("update_url") : null;
    }

    /**
     * Checks the validity of the extension.
     * <p>
     * Used when constructing a extension from a parameter list.
     *
     * @return      True if the extension is valid, false otherwise.
     */
    protected boolean isValid()
    {
        if ( ! _parameters.hasParameter("mode") ||
                ! "fetch_response".equals(_parameters.getParameterValue("mode")))
        {
            _log.warn("Invalid mode value in fetch_reponse: "
                      + _parameters.getParameterValue("mode"));
            return false;
        }

        return super.isValid();
    }
}
