/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.consumer;

import net.openid.discovery.Identifier;
import net.openid.discovery.Discovery;
import net.openid.discovery.DiscoveryInformation;
import net.openid.discovery.DiscoveryException;
import net.openid.message.ax.FetchRequest;
import net.openid.message.ax.FetchResponse;
import net.openid.message.ax.AxMessage;
import net.openid.message.*;
import net.openid.association.AssociationException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Sample Consumer (Relying Party) implementation.
 */
public class SampleConsumer
{
    // instantiate a ConsumerManager object
    public static ConsumerManager manager = new ConsumerManager();


    // --- placing the authentication request ---
    public String authRequest(String userSuppliedString, HttpSession session)
            throws DiscoveryException, ConsumerException,
            AssociationException, MessageException
    {
        // determine a return_to URL where your application will receive
        // the authentication responses from the OpenID provider
        String returnToUrl = "http://example.com/openid";

        // build an Identifier instance from the user-supplied identifier
        Identifier identifier = Discovery.parseIdentifier(userSuppliedString);

        // perform discovery on the user-supplied identifier
        List discoveries = Discovery.discover(identifier);

        // attempt to associate with an OpenID provider
        // and retrieve one service endpoint for authentication
        DiscoveryInformation discovered = manager.associate(discoveries);

        // store the discovery information in the user's session
        session.setAttribute("openid-disco", discovered);

        // Attribute Exchange example: fetching the 'email' attribute
        FetchRequest fetch = new FetchRequest();
        fetch.addAttribute("email",                         // attribute alias
                "http://schema.openid.net/contact/email",   // type URI
                true);                                      // required

        // obtain a AuthRequest message to be sent to the OpenID provider
        AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

        // attach the extension to the authentication request
        authReq.addExtensionParams(fetch);

        // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
        return authReq.getRedirectUrl();

        // Option 2: HTML FORM Redirection
        // <FORM action="OpenID Provider's service endpoint">
        // see samples/formredirection.jsp for a JSP example
        //authReq.getOPEndpoint();

        // build a HTML FORM with the message parameters
        //authReq.getParameterMap();
    }

    public Identifier verifyResponse(HttpServletRequest httpReq, HttpSession session)
            throws AssociationException, MessageException, DiscoveryException
    {
        // --- processing the authentication response

        // extract the parameters from the authentication response
        // (which comes in as a HTTP request from the OpenID provider)
        ParameterList response = new ParameterList(httpReq.getParameterMap());

        // retrieve the previously stored discovery information
        DiscoveryInformation discovered
                = (DiscoveryInformation) session.getAttribute("openid-disco");

        // verify the response; ConsumerManager needs to be the same
        // (static) instance used to place the authentication request
        VerificationResult verification = manager.verify(response, discovered);

        // examine the verification result and extract the verified identifier
        Identifier verified = verification.getVerifiedId();
        if (verified != null)
        {
            // Attribute Exchange example: retrieving the fetched "email" attribute
            AuthSuccess authSuccess = AuthSuccess.createAuthSuccess(response);
            MessageExtension ext =
                    authSuccess.getExtension(AxMessage.OPENID_NS_AX);
            if (ext != null)
            {
                FetchResponse fetchResp =
                        new FetchResponse(ext.getParameters());
                String email = fetchResp.getParameter("email");
            }

            return verified;  // success
        }

        return null;
    }

}
