/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.infocard.rp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.infocard.OpenIDTokenType;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Utility class to generate HTML or XHTLM snippets that express
 * Relying Parties' requirements and invoke Infocard Selectors,
 * requesting login with an OpenID Infocard.
 * <p>
 * Attribute Exchange Fetch Requests can be mapped to Infocard claim URIs.
 *
 * @author Johnny Bufu
 */
public class InfocardInvocation
{
    private static Log _log = LogFactory.getLog(InfocardInvocation.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /**
     * The requested token type (OpenID 1.1 or 2.0)
     */
    private OpenIDTokenType _tokenType;

    /**
     * List of required claim URIs. The OpenID Identifier claim
     * is always part of the list.
     */
    private List _requiredClaims = new ArrayList();

    /**
     * List of optional claim URIs.
     */
    private List _optionalClaims = new ArrayList();

    /**
     * The issuer's URL for the accepted claims.
     */
    private String _issuer;

    /**
     * The issuer's WS-SecurityPolicy URL, if different than "<issuer>/mex".
     */
    private String _issuerPolicy;

    /**
     * Relying Party's privacy URL.
     */
    private String _privacyUrl;

    /**
     * Relying Party's privacy document version. When selectors notice
     * a change in this value, users are prompted with the privacy policy
     * document retrieved from the privacyUrl.
     */
    private int _privacyVersion;

    // todo: enforce data types?

    /**
     * Creates a new InfocardInvocation object, describing Relying Party's
     * requirements.
     *
     * @param tokenType     The required token type.
     */
    public InfocardInvocation(OpenIDTokenType tokenType)
    {
        _requiredClaims.add(OpenIDTokenType.OPENID_CLAIM);
        _tokenType = tokenType;

        if (DEBUG)
            _log.debug("Created " + _tokenType + " token type InfocardInvocation");
    }

    /**
     * Creates an InfocardInvocation object from an Attribute Exchange
     * Fetch Request.
     * <p>
     * Attriute type URIs are mapped to Infocard claim URIs.
     * Attribute value count and update_url features are cannot be
     * expressed in InfocardInvocation data structures.
     *
     * @param fetch     The Fetch Request.
     */
    public InfocardInvocation(FetchRequest fetch)
    {
        _requiredClaims.add(OpenIDTokenType.OPENID_CLAIM);
        _tokenType = OpenIDTokenType.OPENID20_TOKEN;
        
        _requiredClaims.addAll(fetch.getAttributes(true).values());
        _optionalClaims.addAll(fetch.getAttributes(false).values());

        if (DEBUG)
            _log.debug("Created " + _tokenType +
                " token type InfocardInvocation from a FetchRequest.");
    }

    /**
     * Gets the token type.
     */
    public OpenIDTokenType getTokenType()
    {
        return _tokenType;
    }

    /**
     * Sets the token type.
     * @param tokenType
     */
    public void setTokenType(OpenIDTokenType tokenType)
    {
        this._tokenType = tokenType;
    }

    /**
     * Gets required or optional claim URIs.
     * <p>
     * The OpenID Identifier claim is always part of the required claims list.
     *
     * @param required  If true, the required claims are returned; optional
     *                  claims are returned otherwise.
     * @return          The list of configured required/optional claims.
     */
    public List getClaims(boolean required)
    {
        return required ? _requiredClaims : _optionalClaims;
    }

    /**
     * Adds a claim URI to the required or optional claim list.
     *
     * @param claim                 The claim URI to be added.
     * @param required              If true, the clai is added to the required
     *                              claims list, otherwise it is added to the
     *                              optional claims list.
     */
    public void addClaim(String claim, boolean required)
    {
        if (required && ! _requiredClaims.contains(claim))
            _requiredClaims.add(claim);

        else if (! _optionalClaims.contains(claim))
            _optionalClaims.add(claim);
    }

    /**
     * Sets the list of required or optional claim URIs.
     * <p>
     * If the required claim list is set, and the OpenID Identifier claim
     * is not part of the provided list, it is added transparently to the list.
     *
     * @param claims                List of claim URIs.
     * @param required              If true, the required claims list is set,
     *                              otherwise the optional claims list is set.
     */
    public void setClaims(List claims, boolean required)
    {
        if (required)
        {
            _requiredClaims = claims;
            if (! _requiredClaims.contains(OpenIDTokenType.OPENID_CLAIM))
                _requiredClaims.add(OpenIDTokenType.OPENID_CLAIM);
        }
        else
            _optionalClaims = claims;

    }

    /**
     * Gets the issuer URL.
     */
    public String getIssuer()
    {
        return _issuer;
    }

    /**
     * Sets the issuer URL.
     * @param issuer
     */
    public void setIssuer(String issuer)
    {
        this._issuer = issuer;
    }

    /**
     * Gets the issuer policy URL, if different than "<issuer>/mex".
     */
    public String getIssuerPolicy()
    {
        return _issuerPolicy;
    }

    /**
     * Sets the issuer policy URL, if different than "<issuer>/mex".
     */
    public void setIssuerPolicy(String issuerPolicy)
    {
        this._issuerPolicy = issuerPolicy;
    }

    /**
     * Gets the Relyin Party's privacy policy URL.
     */
    public String getPrivacyUrl()
    {
        return _privacyUrl;
    }

    /**
     * Gets the Relying Party's privacy document's version.
     */
    public int getPrivacyVersion()
    {
        return _privacyVersion;
    }

    /**
     * Sets the Relyin Party's privacy policy URL and version.
     * <p>
     * When selectors notice a change in this value, users are prompted
     * with the privacy policy document retrieved from the privacyUrl.
     */
    public void setPrivacyData(String url, int version)
    {
        _privacyUrl = url;
        _privacyVersion = version;
    }

    /**
     * Generates the HTML <object> element used to describe
     * the Relying Party's requirements and invoke the infocard selectors.
     */
    public String getHtmlObject()
    {
        StringBuffer object = new StringBuffer();

        object.append("<OBJECT type=\"application/x-informationCard\" name=\"xmlToken\">");

        object.append(getObjectParam("tokenType", _tokenType.toString()));

        // claims
        object.append(getObjectParam("requiredClaims", arrayToString(_requiredClaims)));

        if (_optionalClaims.size() > 0)
            object.append(getObjectParam("optionslClaims", arrayToString(_optionalClaims)));

        // issuer
        if (_issuer != null && _issuer.length() > 0)
            object.append(getObjectParam("issuer", _issuer));

        if (_issuerPolicy != null && _issuerPolicy.length() > 0)
            object.append(getObjectParam("issuerPolicy", _issuerPolicy));

        // privacy
        if (_privacyUrl != null && _privacyUrl.length() > 0)
        {
            object.append(getObjectParam("privacyUrl", _privacyUrl));
            object.append(getObjectParam("privacyVersion", Integer.toString(_privacyVersion)));
        }

        if (DEBUG)
            _log.debug("Generated <object> element: " + object);

        return object.toString();
    }


    /**
     * Generates the XHTML snippet element used to describe
     * the Relying Party's requirements and invoke the infocard selectors.
     */
    public String getXhtml()
    {
        StringBuffer xhtml = new StringBuffer();

        if (DEBUG)
            _log.debug("Generated XHTML invocation snippet: " + xhtml);

        // todo: xhtml
        throw new UnsupportedOperationException("XHTML invocation not implemented");
    }

    /**
     * Generates an HTML snippet for an <object> parameter
     * from a name-value pair.
     */
    public String getObjectParam(String paramName, String paramValue)
    {
        StringBuffer param = new StringBuffer();

        param.append("<PARAM Name=\"").append(paramName).append("\"");
        param.append(" Value=\"").append(paramValue).append("\"");

        return param.toString();
    }

    /**
     * Converts a List of Strings to a space-separated string.
     */
    public String arrayToString(List list)
    {
        StringBuffer result = new StringBuffer();

        if (list != null && list.size() > 0)
        {
            Iterator iter = list.iterator();
            while (iter.hasNext())
            {
                result.append(iter.next());
                result.append(" ");
            }

            result.deleteCharAt(result.length() - 1);
        }

        return result.toString();
    }
}
