/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.message;

import net.openid.discovery.DiscoveryException;
import net.openid.util.InternetDateFormat;
import net.openid.association.Association;
import net.openid.association.AssociationException;

import java.util.*;
import java.text.ParseException;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class AuthSuccess extends Message
{
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


    protected AuthSuccess(String opEndpoint, String claimedId, String delegate,
                       boolean compatibility,
                       String returnTo, String nonce,
                       String invalidateHandle, Association assoc,
                       String signList)
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
        setSigned(signList);

        setSignature(assoc.sign(getSignedText()));
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
                       String signList)
            throws MessageException, AssociationException
    {
        AuthSuccess resp = new AuthSuccess(opEndpoint, claimedId, delegate,
                compatibility, returnTo, nonce, invalidateHandle, assoc, signList);

        if (! resp.isValid()) throw new MessageException(
                "Invalid set of parameters for the requested message type");

        return resp;
    }

    public static AuthSuccess createAuthSuccess(ParameterList params)
            throws MessageException
    {
        AuthSuccess resp = new AuthSuccess(params);

        if (! resp.isValid()) throw new MessageException(
                "Invalid set of parameters for the requested message type");

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
    }

    public String getReturnTo()
    {
        return getParameterValue("openid.return_to");
    }

    public String getRedirectUrl()
    {
        String return_to = getReturnTo();
        if (return_to == null) return null;

        boolean hasQuery = getReturnTo().indexOf("?") > 0;
        String initialChar = hasQuery ? "&" : "?";

        return return_to + initialChar + wwwFormEncoding();
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

    public void setSigned(String signed)
    {
        set("openid.signed", signed);
    }

    public void setSignature(String sig)
    {
        set("openid.sig", sig);
    }

    public String getSignature()
    {
        return getParameterValue("openid.sig");
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
            return false;

        if ( ! compatibility && ! hasParameter("openid.op_endpoint"))
            return false;

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
            return false;
        }

        if (! MODE_IDRES.equals(getMode()))
            return false;

        if ( compatibility && ! hasParameter("openid.identity") )
            return false;

        // figure out if 'identity' is optional
        if ( ! hasParameter("openid.identity") )
        {
            // not optional in v1
            if (compatibility) return false;

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
                // no extension provides authentication sevices, invalid message
                return false;

            // claimed_id must be present if and only if identity is present
            if ( hasParameter("openid.claimed_id") )
                return false;
        }
        else if ( ! compatibility && ! hasParameter("openid.claimed_id") )
            return false;

        // nonce optional or not?
        String nonce = getNonce();
        if ( !compatibility )
        {
            if (nonce == null) return false;

            // nonce format
            InternetDateFormat _dateFormat = new InternetDateFormat();
            try
            {
                _dateFormat.parse(nonce.substring(0, 20));
            } catch (ParseException e)
            {
                return false;
            }

            if (nonce.length() >255)
                return false;

        } else if (nonce != null)
        {
            return false;
        }

        List signedFields = Arrays.asList(
                getParameterValue("openid.signed").split(","));

        // return_to must be signed
        if (!signedFields.contains("return_to"))
            return false;

        // either compatibility mode or nonce signed
        if ( compatibility == signedFields.contains("response_nonce") )
            return false;

        // either compatibility mode or op_endpoint signed
        if ( compatibility == signedFields.contains("op_endpoint") )
            return false;

        // assoc_handle must be signed in v2
        if ( ! compatibility && ! signedFields.contains("assoc_handle") )
            return false;

        // 'identity' and 'claimed_id' must be signed if present
        if (hasParameter("openid.identity") &&
                ! signedFields.contains("identity"))
            return false;

        if (hasParameter("openid.claimed_id") &&
                ! signedFields.contains("claimed_id"))
            return false;

        return true;
    }
}

