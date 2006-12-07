/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.server;

import net.openid.message.ParameterList;
import net.openid.message.Message;
import net.openid.message.DirectError;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Sample Server (OpenID Provider) implementation.
 */
public class SampleServer
{
    // instantiate a ServerManager object
    public static ServerManager manager = new ServerManager();

    public String processRequest(HttpServletRequest httpReq)
    {
        // extract the parameters from the request
        ParameterList request = new ParameterList(httpReq.getParameterMap());

        String mode = request.hasParameter("openid.mode") ?
                request.getParameterValue("openid.mode") : null;

        Message response;
        String responseText;

        if ("associate".equals(mode))
        {
            // --- process an association request ---
            response = manager.associationResponse(request);
            responseText = response.keyValueFormEncoding();
        } else if ("checkid_setup".equals(mode)
                || "checkid_immediate".equals(mode))
        {
            // interact with the user and obtain data needed to continue
            List userData = userInteraction(request);

            String userSelectedId = (String) userData.get(0);
            String userSelectedClaimedId = (String) userData.get(1);
            Boolean authenticatedAndApproved = (Boolean) userData.get(2);

            // --- process an authentication request ---
            response = manager.authResponse(request,
                    userSelectedId,
                    userSelectedClaimedId,
                    authenticatedAndApproved.booleanValue());
            responseText = response.wwwFormEncoding();
        } else if ("check_authentication".equals(mode))
        {
            // --- processing a verification request ---
            response = manager.verify(request);
            responseText = response.keyValueFormEncoding();
        } else
        {
            // --- error response ---
            response = DirectError.createDirectError("Unknown request");
            responseText = response.keyValueFormEncoding();
        }

        // return the result to the user
        return responseText;
    }

    private List userInteraction(ParameterList request)
    {
        return null;
    }

}
