/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.infocard.sts;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;

import org.eclipse.higgins.sts.api.*;
import org.eclipse.higgins.sts.common.Fault;
import org.eclipse.higgins.sts.utilities.XMLHelper;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.server.ServerAssociationStore;
import org.openid4java.server.JdbcServerAssociationStore;
import org.openid4java.server.NonceGenerator;
import org.openid4java.server.IncrementalNonceGenerator;
import org.openid4java.association.AssociationException;
import org.openid4java.association.Association;
import org.openid4java.OpenIDException;
import org.openid4java.infocard.OpenIDTokenType;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.util.Base64;

import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Handle RSTs and generate RSTRs containing OpenID Tokens.
 *
 * @author Johnny Bufu
 */
public class OpenIDTokenGeneratorHandler
	extends org.eclipse.higgins.sts.server.token.handler.TokenHandler
{
    private final org.eclipse.higgins.sts.utilities.LogHelper log =
        new org.eclipse.higgins.sts.utilities.LogHelper
		(OpenIDTokenGeneratorHandler.class.getName());
	
	javax.xml.namespace.QName qnameIdentityClaimType =
        new javax.xml.namespace.QName(null, "ClaimType");

    javax.xml.namespace.QName qnameIdentityClaimURI =
        new javax.xml.namespace.QName(null, "Uri");
	
    private boolean bConfigured = false;

    // nonce generator not actually used:
    // verification will remove private assoc on first use
    private NonceGenerator _nonceGenerator = new IncrementalNonceGenerator();

    private ServerAssociationStore _privateAssociations;

    private String _opEndpoint;

    private Integer _expireIn;

    /**
	 * Protected constructor, must use TokenGeneratorHandlerFactory
	 */
	protected OpenIDTokenGeneratorHandler()
	{
		this.log.trace("TokenGeneratorHandler::TokenGeneratorHandler");
	}
	
    /* (non-Javadoc)
	 * @see org.eclipse.higgins.sts.IExtension#configure(java.util.Hashtable)
	 */
	public void configure
		(final Map mapGlobalSettings,
		final String strComponentName,
		final Map mapComponentSettings)
	{
		this.log.trace("TokenGeneratorHandler::initialize");

        String tableName = (String) mapComponentSettings.get("AssocTableName");
        JdbcServerAssociationStore privateAssociations =
            new JdbcServerAssociationStore(tableName);

        boolean status = true;
        try
        {
            InitialContext cxt = new InitialContext();
            String dataSourceJndi = (String) mapComponentSettings.get("AssocDataSource");
            DataSource ds = (DataSource) cxt.lookup(dataSourceJndi);
            privateAssociations.setDataSource(ds);
        }
        catch (NamingException e)
        {
            log.error("Unable to load JNDI data source from context.");
            status = false;
        }

        _privateAssociations = privateAssociations;

        java.net.URI opEndpointUri =
            (java.net.URI) mapComponentSettings.get("OPEndpoint");
        _opEndpoint = opEndpointUri != null ? opEndpointUri.toString() : null;

        _expireIn = (Integer) mapComponentSettings.get("AssocExpiry");

        this.bConfigured = status;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.higgins.sts.IExtension#invoke
     */
	public void invoke
		(final java.util.Map mapGlobalSettings,
		final String strComponentName,
		final java.util.Map mapComponentSettings,
		final java.util.Map mapInvocationSettings,
		final IConstants constants,
		final ISTSRequest request,
		final ISTSResponse response)
	{
		this.log.trace("TokenGeneratorHandler::invoke: " + strComponentName);
		
		if (!this.bConfigured)
		{
            setWstFault(constants, response, "The specified request failed",
                "Issue handler not configured");
			return;
		}

        // --- load component configuration ---

        final java.net.URI uriDefaultKeyType =
            (java.net.URI)mapComponentSettings.get("DefaultKeyType");
        this.log.trace("DefaultKeyType: " +
            uriDefaultKeyType != null ? uriDefaultKeyType.toString() : null);
		
		final java.lang.Boolean bIncludeBearerSubjectName =
            (java.lang.Boolean)mapComponentSettings.get("IncludeBearerSubjectName");
		this.log.trace("IncludeBearerSubjectName: " +
            bIncludeBearerSubjectName != null ?
            bIncludeBearerSubjectName.toString() : null);

        final java.net.URI uriTokenIssuer =
            (java.net.URI)mapComponentSettings.get("TokenIssuer");
		this.log.trace("TokenIssuer: " + uriTokenIssuer != null ?
            uriTokenIssuer.toString() : null);

        if (null == uriTokenIssuer)
		{
            setWstFault(constants, response, "The specified request failed",
				"TokenIssuer not set.");
			return;
		}

        final java.net.URI uriSubjectNameIdentifier =
            (java.net.URI)mapComponentSettings.get("SubjectNameIdentifierAttribute");
		if (null != uriSubjectNameIdentifier)
			this.log.trace("SubjectNameIdentifier: " +
                uriSubjectNameIdentifier != null ?
                uriSubjectNameIdentifier.toString() : null);
		
		final java.net.URI uriSubjectNameIdentifierFormat =
            (java.net.URI)mapComponentSettings.get("SubjectNameIdentifierFormat");
		if (null != uriSubjectNameIdentifierFormat)
			this.log.trace("SubjectNameIdentifierFormat: " +
                uriSubjectNameIdentifierFormat != null ?
                uriSubjectNameIdentifierFormat.toString() : null);

        final java.lang.Boolean bEncryptToken =
            (java.lang.Boolean)mapComponentSettings.get("EncryptToken");
		this.log.trace("EncryptToken: " +
            bEncryptToken != null ? bEncryptToken.toString() : null);
	


        // --- extract needed data from the RST ---

        final java.util.List listRST = request.getRequestSecurityTokenCollection();
		final IRequestSecurityToken RST = (IRequestSecurityToken)listRST.get(0);

        final org.eclipse.higgins.sts.api.ILifetime ltLifetime = RST.getLifetime();

        final java.net.URI uriTokenType = RST.getTokenType();
        if (uriTokenType == null ||
            (! OpenIDTokenType.OPENID20_TOKEN.toString().equals(uriTokenType.toString()) &&
             ! OpenIDTokenType.OPENID11_TOKEN.toString().equals(uriTokenType.toString()) ) )
        {
            setWstFault(constants, response, "Invalid token type",
                "Cannot handle tokens of type: " + uriTokenType);
            return;
        }
        boolean compat = OpenIDTokenType.OPENID11_TOKEN.equals(uriTokenType.toString());

        // appliesTo = OpenID return_to URL
		final org.eclipse.higgins.sts.api.IAppliesTo appliesToRequest = RST.getAppliesTo();
		java.net.URI uriAppliesTo = null;
		this.log.trace("Checking for AppliesTo");
		if (appliesToRequest != null)
		{
			this.log.trace("Found AppliesTo");
			final org.eclipse.higgins.sts.api.IEndpointReference eprAppliesTo =
                appliesToRequest.getEndpointReference();
			uriAppliesTo = eprAppliesTo.getAddress();
		}

        if (uriAppliesTo == null)
        {
            setWstFault(constants, response, "The specified request failed",
                "AppliesTo / return_url not found; required for OpenID Tokens.");
            return;
        }

		final org.eclipse.higgins.sts.api.IDigitalIdentity digitalIdentity =
            RST.getDigitalIdentity();
		if (null == digitalIdentity)
	   	{
            setWstFault(constants, response, "The specified request failed",
				"Digital Subject was not found");
			return;
	   	}
		
        // --- build response ---

        final OMFactory omFactory = OMAbstractFactory.getOMFactory();

		final OMNamespace omIdentityNamespace = omFactory.createOMNamespace(
            constants.getIdentityNamespace().toString(),"ic");

        final OMNamespace omWSTrustNamespace = omFactory.createOMNamespace(
            constants.getWSTrustNamespace().toString(),"wst");

        final OMElement omRequestedDisplayToken = omFactory.createOMElement(
            "RequestedDisplayToken", omIdentityNamespace);

        final OMElement omDisplayToken = omFactory.createOMElement(
            "DisplayToken", omIdentityNamespace, omRequestedDisplayToken);

        OMElement omRequestedSecurityToken = omFactory.createOMElement(
            "RequestedSecurityToken", omWSTrustNamespace);

        final org.apache.axiom.om.OMElement omRequestedAttachedReference =
            omFactory.createOMElement("RequestedAttachedReference", omWSTrustNamespace);
        final org.apache.axiom.om.OMElement omRequestedUnattachedReference =
            omFactory.createOMElement("RequestedUnattachedReference", omWSTrustNamespace);

        final org.apache.axiom.om.OMNamespace omWSSNamespace =
            omFactory.createOMNamespace(constants.getWSSecurityNamespace().toString(), "wsse");
        final org.apache.axiom.om.OMElement omSecurityTokenReference1 =
            omFactory.createOMElement("SecurityTokenReference",
                omWSSNamespace, omRequestedAttachedReference);
        final org.apache.axiom.om.OMElement omSecurityTokenReference2 =
            omFactory.createOMElement("SecurityTokenReference",
                omWSSNamespace, omRequestedUnattachedReference);
        final org.apache.axiom.om.OMElement omKeyIdentifier1 =
            omFactory.createOMElement("KeyIdentifier",
                omWSSNamespace, omSecurityTokenReference1);
        final org.apache.axiom.om.OMElement omKeyIdentifier2 =
            omFactory.createOMElement("KeyIdentifier",
                omWSSNamespace, omSecurityTokenReference2);

        String keyIdentifierValueType =
            "http://docs.oasis-open.org/wss/oasis-wss-soap-message-security-1.1#ThumbprintSHA1";
        omKeyIdentifier1.addAttribute("ValueType", keyIdentifierValueType, null);
        omKeyIdentifier2.addAttribute("ValueType", keyIdentifierValueType, null);


        // --- process the claims / attribute request ---

        String claimedID = null;
        Map attrs = new HashMap();

        final java.util.List listClaims = digitalIdentity.getClaims();
        final java.util.Map mapAttributeClaim =
            (java.util.Map)mapGlobalSettings.get("AttributeClaimMap");

        String claimTypeUri;
        String value;
        String displayTag;
        Iterator claimsIter = listClaims.iterator();
        while (claimsIter.hasNext())
        {
            final IClaim claim = (IClaim) claimsIter.next();

            value =  claim.getValues().hasNext() ?
                (String) claim.getValues().next() : null;
            if (value == null) continue;

            claimTypeUri = claim.getType().getName().toString();
            displayTag = (String) ((Map)mapAttributeClaim.get(claimTypeUri)).get("DisplayName");

            if (OpenIDTokenType.OPENID_CLAIM.equals(claimTypeUri))
            {
                claimedID = value;
                addDisplayClaim(claimTypeUri, claimedID, displayTag,
                    omDisplayToken, omIdentityNamespace, omFactory);
                if (compat) break;
            }
            else if (! compat)
            {
                attrs.put(claimTypeUri, value);
                addDisplayClaim(claimTypeUri, value, displayTag,
                    omDisplayToken, omIdentityNamespace, omFactory);
            }
        }

        if (claimedID == null)
        {
            setWstFault(constants, response,
                "Cannot process OpenID-token RST",
                "No claimed identifier found.");
            return;
        }

        Association assoc;
        try
        {
            assoc = _privateAssociations.generate(
                org.openid4java.association.Association.TYPE_HMAC_SHA1, _expireIn.intValue());
        }
        catch (AssociationException e)
        {
            setWstFault(constants, response,
                "Cannot instantiate private association store",
                e.getMessage());
            return;
        }

        if (! compat && _opEndpoint == null)
        {
            setWstFault(constants, response,
                "Cannot process OpenID-token RST",
                "OP-Endpoint not configured; required for OpenID 2 messages.");
            return;
        }

        // nonces not used: OP invalidates private assoc handle on first use
        String nonce = _nonceGenerator.next();

        AuthSuccess openidResp;
        try
        {
            openidResp = AuthSuccess.createAuthSuccess(
            _opEndpoint, claimedID, claimedID,
            compat, uriAppliesTo.toString(), nonce,
            null, assoc, false);

            if (! compat)
            {
                FetchResponse fetchResp = FetchResponse.createFetchResponse();
                fetchResp.addAttributes(attrs);
                openidResp.addExtension(fetchResp);
            }

            // sign the message
            openidResp.setSignature(assoc.sign(openidResp.getSignedText()));
        }
        catch (OpenIDException e)
        {
            setWstFault(constants, response,
                "Cannot generate OpenID assertion",
                e.getMessage());
            return;
        }

        // set the attached / unattached token reference hash
        MessageDigest md;
        try
        {
            md = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e)
        {
            setWstFault(constants, response,
                "Cannot create SHA-1 hash for Requested(Un)AttachedReference",
                e.getMessage());
            return;
        }

        String sha1base64 = null;
        try
        {
            sha1base64 = Base64.encode(
            md.digest(openidResp.keyValueFormEncoding().getBytes("utf-8")));
        }
        catch (UnsupportedEncodingException e)
        {
            setWstFault(constants, response,
                "Unsupported encoding for the OpenID message",
                e.getMessage());
            return;
        }

        omKeyIdentifier1.setText(sha1base64);
        omKeyIdentifier2.setText(sha1base64);

        //todo: move this to OMElement OpenIDToken.getToken()?
        //OpenIDToken openidToken = new OpenIDToken(openidResp);
        final OMNamespace omOpenIDNamespace = omFactory.createOMNamespace(
            org.openid4java.message.Message.OPENID2_NS, "openid");
        OMElement omOpenIDToken = omFactory.createOMElement(
            "OpenIDToken", omOpenIDNamespace, omRequestedSecurityToken);

        omOpenIDToken.setText(openidResp.keyValueFormEncoding());


        final java.util.List listRSTR =
            response.getRequestSecurityTokenResponseCollection();
		if (0 == listRSTR.size())
		{
			listRSTR.add(new org.eclipse.higgins.sts.common.RequestSecurityTokenResponse());
		}

		final org.eclipse.higgins.sts.api.IRequestSecurityTokenResponse RSTR =
            (org.eclipse.higgins.sts.common.RequestSecurityTokenResponse)listRSTR.get(0);

		try
		{
			RSTR.setTokenType(uriTokenType);

            RSTR.setLifetime(ltLifetime);

            RSTR.setRequestedSecurityToken(
                XMLHelper.toElement(omRequestedSecurityToken));

            RSTR.setRequestedDisplayToken(
                XMLHelper.toElement(omRequestedDisplayToken));

            RSTR.setRequestedAttachedReference
                (org.eclipse.higgins.sts.utilities.XMLHelper.toElement(omRequestedAttachedReference));
            RSTR.setRequestedUnattachedReference
                (org.eclipse.higgins.sts.utilities.XMLHelper.toElement(omRequestedUnattachedReference));
            
        }
		catch (final Exception e)
		{
    		org.eclipse.higgins.sts.utilities.ExceptionHelper.Log(this.log,e);

            setWstFault(constants, response, "The specified request failed",
				"Failed to set RequestSecurityToken elements.");
		}
	}

    private void setWstFault(IConstants constants, ISTSResponse response,
                             String reason, String detail)
    {
        final Fault fault = new Fault(
            constants.getWSTrustNamespace(),
            "wst", constants.getRequestFailedFaultCode(),
            reason, detail);

        response.setFault(fault);
    }


    public void addDisplayClaim(String uri, String value, String displayTag,
                                OMElement omParent, OMNamespace omNs, OMFactory omFactory)
    {
        final OMElement elemDisplayClaim = omFactory.createOMElement(
            "DisplayClaim", omNs, omParent);

        elemDisplayClaim.addAttribute("Uri", uri, null);

        // build and set the display tag as the part after the last "/"
        final OMElement elemDisplayTag = omFactory.createOMElement(
            "DisplayTag", omNs, elemDisplayClaim);
        if (displayTag == null || displayTag.length() == 0)
        {
            int lastIndex = uri.lastIndexOf("/");
            displayTag = "";
            if (lastIndex > -1 && uri.length() > lastIndex)
                displayTag = uri.substring(lastIndex + 1);
        }
        elemDisplayTag.setText(displayTag);

        // set the display value
        final OMElement elemDisplayValue = omFactory.createOMElement(
            "DisplayValue", omNs, elemDisplayClaim);
        elemDisplayValue.setText(value);
    }
}