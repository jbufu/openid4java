/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.consumer;

import org.openid4java.discovery.Identifier;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;
import org.openid4java.message.*;
import org.openid4java.OpenIDException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.List;
import java.io.IOException;

/**
 * Sample Consumer (Relying Party) implementation.
 */
public class SampleConsumer
{
    private ConsumerManager manager;
    private String returnToUrl;

    public SampleConsumer() throws ConsumerException
    {
        this("http://example.com/openid");
    }

    public SampleConsumer(String returnToUrl) throws ConsumerException
    {
        // configure the return_to URL where your application will receive
        // the authentication responses from the OpenID provider
        this.returnToUrl = returnToUrl;

        // instantiate a ConsumerManager object
        manager = new ConsumerManager();
        manager.setAssociations(new InMemoryConsumerAssociationStore());
        manager.setNonceVerifier(new InMemoryNonceVerifier(5000));

        // for a working demo, not enforcing RP realm discovery
        // since this new feature is not deployed
        manager.getRealmVerifier().setEnforceRpId(false);
    }

    // --- placing the authentication request ---
    public String authRequest(String userSuppliedString,
                              HttpServletRequest httpReq,
                              HttpServletResponse httpResp)
            throws IOException
    {
        try
        {

            // --- Forward proxy setup (only if needed) ---
            // ProxyProperties proxyProps = new ProxyProperties();
            // proxyProps.setProxyName("proxy.example.com");
            // proxyProps.setProxyPort(8080);
            // HttpClientFactory.setProxyProperties(proxyProps);

            // perform discovery on the user-supplied identifier
            List discoveries = manager.discover(userSuppliedString);

            // attempt to associate with the OpenID provider
            // and retrieve one service endpoint for authentication
            DiscoveryInformation discovered = manager.associate(discoveries);

            // store the discovery information in the user's session
            httpReq.getSession().setAttribute("openid-disc", discovered);

            // obtain a AuthRequest message to be sent to the OpenID provider
            AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

            // Attribute Exchange example: fetching the 'email' attribute
            FetchRequest fetch = FetchRequest.createFetchRequest();
            fetch.addAttribute("email", // attribute alias
                "http://schema.openid.net/contact/email", // type URI
                true); // required
            // attach the extension to the authentication request
            authReq.addExtension(fetch);

            // example using Simple Registration to fetching the 'email' attribute
            SRegRequest sregReq = SRegRequest.createFetchRequest();
            sregReq.addAttribute("email", true);
            authReq.addExtension(sregReq);

            if (! discovered.isVersion2() )
            {
                // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
                // The only method supported in OpenID 1.x
                // redirect-URL usually limited ~2048 bytes
                httpResp.sendRedirect(authReq.getDestinationUrl(true));
                return null;
            }
            else
            {
                // Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)

                //RequestDispatcher dispatcher =
                //        getServletContext().getRequestDispatcher("formredirection.jsp");
                //httpReq.setAttribute("prameterMap", response.getParameterMap());
                //httpReq.setAttribute("destinationUrl", response.getDestinationUrl(false));
                //dispatcher.forward(request, response);
            }
        }
        catch (OpenIDException e)
        {
            // present error to the user
            throw new RuntimeException("wrap:" + e.getMessage(), e);
        }

        return null;
    }

    // --- processing the authentication response ---
    public Identifier verifyResponse(HttpServletRequest httpReq)
    {
        try
        {
            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList response =
                    new ParameterList(httpReq.getParameterMap());

            // retrieve the previously stored discovery information
            DiscoveryInformation discovered = (DiscoveryInformation)
                    httpReq.getSession().getAttribute("openid-disc");

            // extract the receiving URL from the HTTP request
            StringBuffer receivingURL = httpReq.getRequestURL();
            String queryString = httpReq.getQueryString();
            if (queryString != null && queryString.length() > 0)
                receivingURL.append("?").append(httpReq.getQueryString());

            // verify the response; ConsumerManager needs to be the same
            // (static) instance used to place the authentication request
            VerificationResult verification = manager.verify(
                    receivingURL.toString(),
                    response, discovered);

            // examine the verification result and extract the verified identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null)
            {
                AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

                HttpSession session = httpReq.getSession(true);
                session.setAttribute("openid_identifier", authSuccess.getIdentity());

                if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX))
                {
                    FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
                    session.setAttribute("emailFromFetch", fetchResp.getAttributeValues("email").get(0));
                }
                if (authSuccess.hasExtension(SRegMessage.OPENID_NS_SREG))
                {
                    SRegResponse sregResp = (SRegResponse) authSuccess.getExtension(SRegMessage.OPENID_NS_SREG);
                    session.setAttribute("emailFromSReg", sregResp.getAttributeValue("email"));
                }
                return verified;  // success
            }
        }
        catch (OpenIDException e)
        {
            // present error to the user
            throw new RuntimeException("wrap:" + e.getMessage(), e);
        }

        return null;
    }
}
