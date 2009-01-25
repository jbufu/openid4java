/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.samples;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInfoServlet extends HttpServletSupport
{
    private static final long serialVersionUID = 1L;

    protected void onService(HttpServletRequest req, HttpServletResponse resp) throws Exception
    {
        String serverUrl = "http://" + req.getServerName() + ":" + req.getServerPort() + "/provider";
        String back;
        if ("html".equals(req.getParameter("format")))
        {
            resp.setContentType("text/html");
            back = "<html><head>\n" +
                    "<link rel='openid.server' href='" + serverUrl + "'/>\n" +
                    "</head><body>in html</body></html>"
                    ;
        }
        else
        {
            resp.setContentType("application/xrds+xml");
            back = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<xrds:XRDS\n" +
                    "  xmlns:xrds=\"xri://$xrds\"\n" +
                    "  xmlns:openid=\"http://openid.net/xmlns/1.0\"\n" +
                    "  xmlns=\"xri://$xrd*($v*2.0)\">\n" +
                    "  <XRD>\n" +
                    "    <Service priority=\"0\">\n" +
                    "      <Type>http://openid.net/signon/1.0</Type>\n" +
                    "      <URI>http://" + req.getServerName() + ":" + req.getServerPort() + "/provider</URI>\n" +
                    "    </Service>\n" +
                    "  </XRD>\n" +
                    "</xrds:XRDS>"
                    ;
        }
        PrintWriter out = resp.getWriter();
        out.write(back);
    }
}
