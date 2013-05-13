/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;
import org.openid4java.association.Association;
import org.openid4java.server.RealmVerifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class AuthRequest extends Message
{
    private static Log _log = LogFactory.getLog(AuthRequest.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    public static final String MODE_SETUP = "checkid_setup";
    public static final String MODE_IMMEDIATE = "checkid_immediate";

    public static final String SELECT_ID =
            "http://specs.openid.net/auth/2.0/identifier_select";

    protected final static List requiredFields = Arrays.asList( new String[] {
            "openid.mode"
    });

    protected final static List optionalFields = Arrays.asList( new String[] {
            "openid.ns",
            "openid.claimed_id",
            "openid.identity",
            "openid.assoc_handle",
            "openid.realm",
            "openid.trust_root",
            "openid.return_to"
    });

    private RealmVerifier _realmVerifier;

    protected AuthRequest(String claimedId, String delegate, boolean compatibility,
                          String returnToUrl, String handle, RealmVerifier verifier)
    {
        this(claimedId, delegate, compatibility,
                returnToUrl, handle, returnToUrl, verifier);

    }

    protected AuthRequest(String claimedId, String delegate, boolean compatibility,
                          String returnToUrl, String handle, String realm,
                          RealmVerifier verifier)
    {
        if (! compatibility)
        {
            set("openid.ns", OPENID2_NS);
            setClaimed(claimedId);
        }

        setIdentity(delegate);

        if ( returnToUrl != null ) setReturnTo(returnToUrl);
        if ( realm != null ) setRealm(realm);

        if (! Association.FAILED_ASSOC_HANDLE.equals(handle)) setHandle(handle);
        setImmediate(false);

        _realmVerifier = verifier;
    }

    protected AuthRequest(ParameterList params)
    {
        super(params);
    }

    public static AuthRequest createAuthRequest(String claimedId, String delegate,
                                                boolean compatibility, String returnToUrl,
                                                String handle, RealmVerifier verifier)
            throws MessageException
    {
        return createAuthRequest(claimedId, delegate, compatibility,
                returnToUrl, handle, returnToUrl, verifier);
    }

    public static AuthRequest createAuthRequest(String claimedId, String delegate,
                                                boolean compatibility, String returnToUrl,
                                                String handle, String realm, RealmVerifier verifier)
            throws MessageException
    {
        AuthRequest req = new AuthRequest(claimedId, delegate, compatibility,
                returnToUrl, handle, realm, verifier);

        req.validate();

        if (DEBUG) _log.debug("Created auth request:\n" + req.keyValueFormEncoding());

        return req;
    }

    public static AuthRequest createAuthRequest(ParameterList params,
                                                RealmVerifier realmVerifier)
            throws MessageException
    {
        AuthRequest req = new AuthRequest(params);

        req.setRealmVerifier(realmVerifier);

        req.validate();

        if (DEBUG) _log.debug("Created auth request:\n" + req.keyValueFormEncoding());

        return req;
    }

    public List getRequiredFields()
    {
        return requiredFields;
    }

    public void setOPEndpoint(URL opEndpoint)
    {
        if (opEndpoint != null)
            _destinationUrl = opEndpoint.toString();
    }

    public String getOPEndpoint()
    {
        return _destinationUrl;
    }

    public void setImmediate(boolean immediate)
    {
        set("openid.mode", immediate ? MODE_IMMEDIATE : MODE_SETUP);

        if (DEBUG && immediate)
            _log.debug("Setting checkid_immediate auth request.");
    }

    public boolean isImmediate()
    {
        return MODE_IMMEDIATE.equals(getParameterValue("openid.mode"));
    }

    public boolean isVersion2()
    {
        return hasParameter("openid.ns") &&
                OPENID2_NS.equals(getParameterValue("openid.ns"));
    }

    public void setIdentity(String id)
    {
        set("openid.identity", id);
    }

    public String getIdentity()
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

    public void setHandle(String handle)
    {
        set("openid.assoc_handle", handle);
    }

    public String getHandle()
    {
        return getParameterValue("openid.assoc_handle");
    }

    public void setReturnTo(String returnTo)
    {
        set("openid.return_to", returnTo);
    }

    public String getReturnTo()
    {
        return getParameterValue("openid.return_to");
    }

    public void setRealm(String realm)
    {
        set(isVersion2() ? "openid.realm" : "openid.trust_root", realm);
    }

    public String getRealm()
    {
        if (isVersion2())
            return getParameterValue("openid.realm");
        else
            return getParameterValue("openid.trust_root");
    }

    /**
     * Gets the RealmVerifier used to verify realms against return_to URLs.
     */
    public RealmVerifier getRealmVerifier()
    {
        return _realmVerifier;
    }

    /**
     * Sets the RealmVerifier used to verify realms against return_to URLs.
     */
    public void setRealmVerifier(RealmVerifier realmVerifier)
    {
        this._realmVerifier = realmVerifier;
    }

    public void validate() throws MessageException
    {
        super.validate();

        boolean compatibility = ! isVersion2();

        if ( compatibility && hasParameter("openid.identity")  &&
                SELECT_ID.equals(getParameterValue("openid.identity")))
        {
            throw new MessageException(SELECT_ID + " not supported in OpenID1",
                OpenIDException.AUTH_ERROR);
        }

        if ( hasParameter("openid.mode") &&
                ! MODE_SETUP.equals(getParameterValue("openid.mode")) &&
                ! MODE_IMMEDIATE.equals(getParameterValue("openid.mode")))
        {
            throw new MessageException(
                "Invalid openid.mode value in auth request: "
                + getParameterValue("openid.mode"),
                OpenIDException.AUTH_ERROR);
        }

        // return_to must be a valid URL, if present
        try
        {
            if (getReturnTo() != null)
                new URL(getReturnTo());
        } catch (MalformedURLException e)
        {
            throw new MessageException(
                "Error verifying return URL in auth request.",
                OpenIDException.AUTH_ERROR, e);
        }

        if ( ! hasParameter("openid.return_to") )
        {
            if (compatibility)
            {
                throw new MessageException(
                    "openid.return_to is mandatory in OpenID1 auth requests",
                    OpenIDException.AUTH_ERROR);
            }

            else if ( ! hasParameter("openid.realm") )
            {
                throw new MessageException(
                    "openid.realm is mandatory if return_to is absent.",
                    OpenIDException.AUTH_REALM_ERROR);
            }
        }

        if ( compatibility && hasParameter("openid.realm") )
        {
            _log.warn("openid.realm should not be present in OpenID1 auth requests");
        }

        if ( !compatibility && hasParameter("openid.trust_root") )
        {
            _log.warn("openid.trust_root should not be present in OpenID2 auth requests.");
        }

        // figure out if 'claimed_id' and 'identity' are optional
        if ( ! hasParameter("openid.identity") )
        {
            // not optional in v1
            if (compatibility)
            {
                throw new MessageException(
                    "openid.identity is required in OpenID1 auth requests",
                    OpenIDException.AUTH_ERROR);
            }

            boolean hasAuthProvider = false;

            Iterator iter = getExtensions().iterator();
            while (iter.hasNext())
            {
                String typeUri = iter.next().toString();

                try
                {
                    MessageExtension extension = getExtension(typeUri);

                    if (extension.providesIdentifier())
                    {
                        hasAuthProvider = true;
                        break;
                    }
                }
                catch (MessageException ignore)
                {
                    // do nothing
                }
            }

            // no extension provides authentication sevices - invalid message
            if ( !hasAuthProvider )
            {
                throw new MessageException(
                    "no identifier specified in auth request",
                    OpenIDException.AUTH_ERROR);
            }

            // claimed_id must be present if and only if identity is present
            if ( hasParameter("openid.claimed_id") )
            {
                throw new MessageException(
                    "openid.claimed_id must be present if and only if " +
                    "openid.identity is present.",
                    OpenIDException.AUTH_ERROR);
            }
        }
        else if ( ! compatibility && ! hasParameter("openid.claimed_id") )
        {
            throw new MessageException(
                "openid.claimed_id must be present in OpenID2 auth requests",
                OpenIDException.AUTH_ERROR);
        }

        if (getRealm() != null)
        {
            int validation = _realmVerifier.validate(
                                getRealm(), getReturnTo(), compatibility);

            if ( RealmVerifier.OK != validation )
            {
                throw new MessageException("Realm verification failed (" +
                    validation + ") for: " + getRealm(), 
                    OpenIDException.AUTH_REALM_ERROR);
            }
        }
    }
}
