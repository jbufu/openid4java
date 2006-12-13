/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.consumer;

import net.openid.discovery.Identifier;
import net.openid.discovery.DiscoveryInformation;
import net.openid.message.ax.FetchRequest;
import net.openid.message.ax.FetchResponse;
import net.openid.message.ax.AxMessage;
import net.openid.message.*;
import net.openid.OpenIDException;

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
    {
        try
        {
            // determine a return_to URL where your application will receive
            // the authentication responses from the OpenID provider
            String returnToUrl = "http://example.com/openid";

            // perform discovery on the user-supplied identifier
            List discoveries = manager.discover(userSuppliedString);

            // attempt to associate with an OpenID provider
            // and retrieve one service endpoint for authentication
            DiscoveryInformation discovered = manager.associate(discoveries);

            // store the discovery information in the user's session
            session.setAttribute("openid-disco", discovered);

            // Attribute Exchange example: fetching the 'email' attribute
            FetchRequest fetch = FetchRequest.createFetchRequest();
            fetch.addAttribute("email",
                    // attribute alias
                    "http://schema.openid.net/contact/email",   // type URI
                    true);                                      // required

            // obtain a AuthRequest message to be sent to the OpenID provider
            AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

            // attach the extension to the authentication request
            authReq.addExtension(fetch);

            if (! discovered.isVersion2() )
            {
                // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
                // The only method supported in OpenID 1.x
                // redirect-URL usually limited to 255 bytes
                return authReq.getRedirectUrl();
            }
            else
            {
                // Option 2: HTML FORM Redirection
                // Allows payloads > 255 bytes

                // <FORM action="OpenID Provider's service endpoint">
                // see samples/formredirection.jsp for a JSP example
                authReq.getOPEndpoint();

                // build a HTML FORM with the message parameters
                authReq.getParameterMap();
            }
        }
        catch (OpenIDException e)
        {
            // present error to the user
        }

        return null;
    }

    public Identifier verifyResponse(HttpServletRequest httpReq, HttpSession session)
    {
        try
        {
            // --- processing the authentication response

            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList response =
                    new ParameterList(httpReq.getParameterMap());

            // retrieve the previously stored discovery information
            DiscoveryInformation discovered
                    =
                    (DiscoveryInformation) session.getAttribute("openid-disco");

            // verify the response; ConsumerManager needs to be the same
            // (static) instance used to place the authentication request
            VerificationResult verification = manager.verify(
                    httpReq.getRequestURL().toString(),
                    response, discovered);

            // examine the verification result and extract the verified identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null)
            {
                AuthSuccess authSuccess =
                        AuthSuccess.createAuthSuccess(response);

                if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX))
                {
                    FetchResponse fetchResp = (FetchResponse) authSuccess
                            .getExtension(AxMessage.OPENID_NS_AX);

                    List emails = fetchResp.getAttributeValues("email");
                    String email = (String) emails.get(0);
                }

                return verified;  // success
            }

        }
        catch (OpenIDException e)
        {
            // present error to the user
        }

        return null;
    }
}
