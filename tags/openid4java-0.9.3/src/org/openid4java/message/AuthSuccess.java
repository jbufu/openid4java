/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.message;

import org.openid4java.discovery.DiscoveryException;
import org.openid4java.util.InternetDateFormat;
import org.openid4java.association.Association;
import org.openid4java.association.AssociationException;

import java.util.*;
import java.text.ParseException;
import java.net.URL;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class AuthSuccess extends Message
{
    private static Logger _log = Logger.getLogger(AuthSuccess.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    protected final static List requiredFields = Arrays.asList( new String[] {
            "openid.mode",
            "openid.return_to",
            "openid.assoc_handle",
            "openid.signed",
            "openid.sig"
    });

    protected final static List optionalFields = Arrays.asList( new String[] {
            "openid.ns",
            "openid.op_endpoint",
            "openid.claimed_id",
            "openid.identity",
            "openid.response_nonce",
            "openid.invalidate_handle"
    });

    // required signed list in OpenID 1.x
    protected final static String signRequired1 = "return_to,identity";

    // required signed list in OpenID 2.0 with claimed identifier
    protected final static String signRequired2 =
            "op_endpoint,claimed_id,identity,return_to,response_nonce,assoc_handle";

    // required signed list in OpenID 2.0 with no claimed identifier
    protected final static String signRequired3 =
            "op_endpoint,return_to,response_nonce,assoc_handle";

    protected List _signFields = new ArrayList();

    protected List _signExtensions = new ArrayList();

    protected AuthSuccess(String opEndpoint, String claimedId, String delegate,
                          boolean compatibility,
                          String returnTo, String nonce,
                          String invalidateHandle, Association assoc,
                          boolean signNow)
            throws AssociationException
    {
        if (! compatibility)
        {
            set("openid.ns", OPENID2_NS);
            setOpEndpoint(opEndpoint);
            setClaimed(claimedId);
            setNonce(nonce);
        }

        set("openid.mode", MODE_IDRES);

        setIdentity(delegate);
        setReturnTo(returnTo);
        if (invalidateHandle != null) setInvalidateHandle(invalidateHandle);
        setHandle(assoc.getHandle());

        buildSignedList();
        setSignature(signNow ? assoc.sign(getSignedText()) : "");
    }

    protected AuthSuccess(ParameterList params)
    {
        super(params);
    }

    public static AuthSuccess createAuthSuccess(
                       String opEndpoint, String claimedId, String delegate,
                       boolean compatibility,
                       String returnTo, String nonce,
                       String invalidateHandle, Association assoc,
                       boolean signNow)
            throws MessageException, AssociationException
    {
        AuthSuccess resp = new AuthSuccess(opEndpoint, claimedId, delegate,
                                compatibility, returnTo, nonce,
                                invalidateHandle, assoc, signNow);

        if (! resp.isValid()) throw new MessageException(
                "Invalid set of parameters for the requested message type");

        if (DEBUG) _log.debug("Created positive auth response:\n"
                              + resp.keyValueFormEncoding());

        return resp;
    }

    public static AuthSuccess createAuthSuccess(ParameterList params)
            throws MessageException
    {
        AuthSuccess resp = new AuthSuccess(params);

        if (! resp.isValid()) throw new MessageException(
                "Invalid set of parameters for the requested message type");

        if (DEBUG) _log.debug("Created positive auth response:\n"
                              + resp.keyValueFormEncoding());

        return resp;
    }

    public List getRequiredFields()
    {
        return requiredFields;
    }

    public boolean isVersion2()
    {
        return hasParameter("openid.ns")
                && OPENID2_NS.equals(getParameterValue("openid.ns"));
    }

    public void setMode(String mode) throws MessageException
    {
        if (! mode.equals(MODE_IDRES) && ! mode.equals(MODE_CANCEL))
            throw new MessageException("Unknown authentication mode: " + mode);

        set("openid.mode", mode);
    }

    public String getMode()
    {
        return getParameterValue("openid.mode");
    }

    public void setOpEndpoint(String opEndpoint)
    {
        set("openid.op_endpoint", opEndpoint);
    }

    public String getOpEndpoint()
    {
        return getParameterValue("openid.op_endpoint");
    }

    public void setIdentity(String id)
    {
        set("openid.identity", id);
    }

    public String getIdentity() throws DiscoveryException
    {
        return getParameterValue("openid.identity");
    }

    public void setClaimed(String claimed)
    {
        set("openid.claimed_id", claimed);
    }

    public String getClaimed()
    {
        return getParameterValue("openid.claimed_id");
    }

    public void setReturnTo(String returnTo)
    {
        set("openid.return_to", returnTo);
        _destinationUrl = returnTo;
    }

    public String getReturnTo()
    {
        return getParameterValue("openid.return_to");
    }

    public void setNonce(String nonce)
    {
        set("openid.response_nonce", nonce);
    }

    public String getNonce()
    {
        return getParameterValue("openid.response_nonce");
    }

    public void setInvalidateHandle(String handle)
    {
        set("openid.invalidate_handle", handle);
    }

    public String getInvalidateHandle()
    {
        return getParameterValue("openid.invalidate_handle");
    }

    public void setHandle(String handle)
    {
        set("openid.assoc_handle", handle);
    }

    public String getHandle()
    {
        return getParameterValue("openid.assoc_handle");
    }


    /**
     * Builds the list of fields that will be signed. Three input sources are
     * considered for this:
     * <ul>
     * <li>fields required to be signed by the OpenID protocol</li>
     * <li>the user defined list of fields to be signed
     *     {@link #setSignFields(String)}</li>
     * <li>fields belonging to extensions to be signed
     *     {@link #setSignExtensions(String[])}</li>
     * </ul>
     * <p>
     * This method should be called after any field additions/deletions to/from
     * the message.
     */
    public void buildSignedList()
    {
        StringBuffer toSign = ! isVersion2() ? new StringBuffer(signRequired1) :
               hasParameter("openid.identity") ? new StringBuffer(signRequired2)
               : new StringBuffer(signRequired3);

        List signList = new ArrayList(Arrays.asList(toSign.toString().split(",")));

        Iterator iter = _signFields.iterator();
        while (iter.hasNext())
        {
            String field = (String) iter.next();
            if ( ! signList.contains(field) )
            {
                toSign.append(",").append(field);
                signList.add(field);
            }
        }

        // build list of field prefixes belonging to extensions
        List extensionPrefixes = new ArrayList();
        iter = _signExtensions.iterator();
        while(iter.hasNext())
        {
            String alias = getExtensionAlias((String) iter.next());
            if (alias != null)
            {
                // openid.ns.<ext_alias> needs to be signed
                //String nsSign = "ns." + alias;
                //toSign.append(",").append(nsSign);
                //signList.add(nsSign);

                extensionPrefixes.add(alias);
            }
        }

        // add exension fields to the signed list
        iter = getParameters().iterator();
        while(iter.hasNext())
        {
            String paramName = ((Parameter) iter.next()).getKey();

            if (! paramName.startsWith("openid.")) continue;

            String signName = paramName.substring(7);

            int dotIndex = signName.indexOf(".");
            if (dotIndex > 0 &&
                extensionPrefixes.contains(signName.substring(0,dotIndex)) &&
                ! signList.contains(signName) )
            {
                toSign.append(",").append(signName);
                signList.add(signName);
            }
        }

        if (DEBUG) _log.debug("Setting fields to be signed: " + toSign);

        set("openid.signed", toSign.toString());

        //todo: if signature is alread set, recompute it
    }

    /**
     * Sets the messages fields that will be signed, in addition to the ones
     * required by the protocol to be signed. The OpenID signature will
     * only be applied to OpenID fields, starting with the "openid." prefix.
     *
     * @param userSuppliedList  Comma-separated list of fields to be signed,
     *                          without the "openid." prefix
     * @see #setSignExtensions(String[])
     */
    public void setSignFields(String userSuppliedList)
    {
        if (userSuppliedList != null)
        {
            _signFields = Arrays.asList(userSuppliedList.split(","));

            buildSignedList();
        }
    }

    /**
     * Sets the list of messages fields that will be signed, in addition to
     * the ones required by the protocol to be signed and any additional
     * fields already configured to be signed. The OpenID signature will
     * only be applied to OpenID fields, starting with the "openid." prefix.
     * Should be called <b>after</b> all relevant extension fields have been
     * added to the message.
     *
     * @param extensions  Array of extension namespace URIs to be signed.
     * @see #setSignFields(String) #setSignExtension
     */
    public void setSignExtensions(String[] extensions)
    {
        if (extensions != null)
        {
            _signExtensions = Arrays.asList(extensions);

            buildSignedList();
        }
    }


    /**
     * Sets the list of messages fields that will be signed, in addition to
     * the ones required by the protocol to be signed and any additional
     * fields already configured to be signed. The OpenID signature will
     * only be applied to OpenID fields, starting with the "openid." prefix.
     * Should be called <b>after</b> all relevant extension fields have been
     * added to the message.
     *
     * @param extensionNamespace  Extension namespace URI to be signed.
     * @see #setSignFields(String) #setSignExtensions
     */
    public void setSignExtension(String extensionNamespace)
    {
        if (! _signExtensions.contains(extensionNamespace))
        {
            _signExtensions.add(extensionNamespace);

            buildSignedList();
        }
    }

    public List getSignExtensions()
    {
        return _signExtensions;
    }

    public void setSignature(String sig)
    {
        set("openid.sig", sig);

        if(DEBUG) _log.debug("Added signature: " + sig);
    }

    public String getSignature()
    {
        return getParameterValue("openid.sig");
    }

    public String getSignList()
    {
        return getParameterValue("openid.signed");
    }

    /**
     * Return the text on which the signature is applied.
     */
    public String getSignedText()
    {
        StringBuffer signedText = new StringBuffer("");

        String[] signedParams = getParameterValue("openid.signed").split(",");

        for (int i = 0; i < signedParams.length; i++)
        {
            signedText.append(signedParams[i]);
            signedText.append(':');
            signedText.append(getParameterValue("openid." + signedParams[i]));
            signedText.append('\n');
        }

        return signedText.toString();
    }

    public boolean isValid()
    {
        if (!super.isValid()) return false;

        boolean compatibility = ! isVersion2();

        if ( compatibility && hasParameter("openid.ns") )
        {
            _log.warn("openid.ns should not be present in OpenID1 auth responses");
            return false;
        }

        if ( ! compatibility && ! hasParameter("openid.op_endpoint"))
        {
            _log.warn("openid.op_endpoint is required in OpenID auth responses");
            return false;
        }

        try
        {
            // return_to must be a valid URL, if present
            if (getReturnTo() != null)
                new URL(getReturnTo());

            // op_endpoint must be a valid URL, if present
            if (getOpEndpoint() != null)
                new URL(getOpEndpoint());
        }
        catch (MalformedURLException e)
        {
            _log.error("Error verifying auth response validity.", e);
            return false;
        }

        if (! MODE_IDRES.equals(getMode()))
        {
            _log.warn("Invalid openid.mode value in auth response: " + getMode());
            return false;
        }

        // figure out if 'identity' is optional
        if ( ! hasParameter("openid.identity") )
        {
            // not optional in v1
            if (compatibility)
            {
                _log.warn("openid.identity is required in OpenID1 auth responses");
                return false;
            }

            boolean hasAuthExt = false;
            Iterator iter = getExtensions().iterator();
            while (iter.hasNext())
            {
                String typeUri = iter.next().toString();

                try
                {
                    MessageExtension extension = getExtension(typeUri);

                    if (extension.providesIdentifier())
                    {
                        hasAuthExt = true;
                        break;
                    }
                }
                catch (MessageException ignore)
                {
                    // do nothing
                }
            }
            if (! hasAuthExt)
            {
                // no extension provides authentication sevices, invalid message
                _log.warn("no identifier specified in auth request");
                return false;
            }

            // claimed_id must be present if and only if identity is present
            if ( hasParameter("openid.claimed_id") )
            {
                _log.warn("openid.claimed_id must be present if and only if " +
                          "openid.identity is present.");
                return false;
            }
        }
        else if ( ! compatibility && ! hasParameter("openid.claimed_id") )
        {
            _log.warn("openid.clamied_id must be present in OpenID2 auth responses");
            return false;
        }

        // nonce optional or not?
        String nonce = getNonce();
        if ( !compatibility )
        {
            if (nonce == null)
            {
                _log.warn("openid.response_nonce is required in OpenID2 auth responses");
                return false;
            }

            // nonce format
            InternetDateFormat _dateFormat = new InternetDateFormat();
            try
            {
                _dateFormat.parse(nonce.substring(0, 20));
            }
            catch (ParseException e)
            {
                _log.error("Error parsing nonce in auth response.", e);
                return false;
            }

            if (nonce.length() >255)
            {
                _log.warn("nonce length must not exceed 255 characters");
                return false;
            }

        } else if (nonce != null)
        {
            _log.warn("openid.response_nonce present in OpenID1 auth response");
//            return false;
        }

        List signedFields = Arrays.asList(
                getParameterValue("openid.signed").split(","));

        // return_to must be signed
        if (!signedFields.contains("return_to"))
        {
            _log.warn("return_to must be signed");
            return false;
        }

        // either compatibility mode or nonce signed
        if ( compatibility == signedFields.contains("response_nonce") )
        {
            _log.warn("response_nonce must be present and signed only in OpenID2 auth responses");
//            return false;
        }

        // either compatibility mode or op_endpoint signed
        if ( compatibility == signedFields.contains("op_endpoint") )
        {
            _log.warn("op_endpoint must be present and signed only in OpenID2 auth responses");
//            return false;
        }

        // assoc_handle must be signed in v2
        if ( ! compatibility && ! signedFields.contains("assoc_handle") )
        {
            _log.warn("assoc_handle must be signed in OpenID2 auth responses");
            return false;
        }

        // 'identity' and 'claimed_id' must be signed if present
        if (hasParameter("openid.identity") &&
                ! signedFields.contains("identity"))
        {
            _log.warn("openid.identity must be signed if present");
            return false;
        }

        if (hasParameter("openid.claimed_id") &&
                ! signedFields.contains("claimed_id"))
        {
            _log.warn("openid.claimed_id must be signed if present");
            return false;
        }

        return true;
    }
}

