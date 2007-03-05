/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.message;

import net.openid.association.Association;
import net.openid.server.RealmVerifier;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class AuthRequest extends Message
{
    private static Logger _log = Logger.getLogger(AuthRequest.class);
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

        if (! req.isValid()) throw new MessageException(
                "Invalid set of parameters for the requested message type");

        if (DEBUG) _log.debug("Created auth request: " + req.keyValueFormEncoding());

        return req;
    }

    public static AuthRequest createAuthRequest(ParameterList params,
                                                RealmVerifier realmVerifier)
            throws MessageException
    {
        AuthRequest req = new AuthRequest(params);

        req.setRealmVerifier(realmVerifier);

        if (! req.isValid()) throw new MessageException(
                "Invalid set of parameters for the requested message type");

        if (DEBUG) _log.debug("Created auth request: " + req.keyValueFormEncoding());

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

    public boolean isValid()
    {
        if (! super.isValid()) return false;

        boolean compatibility = ! isVersion2();

        if ( compatibility && hasParameter("openid.ns") )
        {
            _log.warn("Invalid value for openid.ns field: "
                      + getParameterValue("openid.ns"));
            return false;
        }

        if ( compatibility && hasParameter("openid.identity")  &&
                SELECT_ID.equals(getParameterValue("openid.identity")))
        {
            _log.warn(SELECT_ID + " not supported in OpenID1");
            return false;
        }

        if ( hasParameter("openid.mode") &&
                ! MODE_SETUP.equals(getParameterValue("openid.mode")) &&
                ! MODE_IMMEDIATE.equals(getParameterValue("openid.mode")))
        {
            _log.warn("Invalid openid.mode value in auth request: "
                      + getParameterValue("openid.mode"));
            return false;
        }

        // return_to must be a valid URL, if present
        try
        {
            if (getReturnTo() != null)
                new URL(getReturnTo());
        } catch (MalformedURLException e)
        {
            _log.error("Error verifying return URL in auth request.", e);
            return false;
        }

        if ( ! hasParameter("openid.return_to") )
        {
            if (compatibility)
            {
                _log.warn("openid.return_to is mandatory in OpenID1 auth requests");
                return false;
            }

            else if ( ! hasParameter("openid.realm") )
            {
                _log.warn("openid.realm is mandatory in Openid2 auth requests");
                return false;
            }
        }

        if ( compatibility && hasParameter("openid.realm") )
        {
            _log.warn("openid.realm should not be present in OpenID1 auth requests");
            return false;
        }

        if ( !compatibility && hasParameter("openid.trust_root") )
        {
            _log.warn("openid.trust_root should not be present in OpenID2 auth requests.");
            return false;
        }

        // figure out if 'claimed_id' and 'identity' are optional
        if ( ! hasParameter("openid.identity") )
        {
            // not optional in v1
            if (compatibility)
            {
                _log.warn("openid.identity is required in OpenID1 auth requests");
                return false;
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
            _log.warn("openid.clamied_id must be present in OpenID2 auth requests");
            return false;
        }

        if (getRealm() != null &&  RealmVerifier.OK !=
                        _realmVerifier.match(getRealm(), getReturnTo()) )
        {
            _log.warn("Realm verification failed for: " + getRealm());
            return false;
        }

        return true;
    }
}
