/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.samples;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.server.SampleServer;

public class ProviderServlet extends HttpServletSupport
{
    private static final long serialVersionUID = 1L;
    private SampleServer server_;

    public ProviderServlet(SampleServer server)
    {
        server_ = server;
    }

    protected void onService(HttpServletRequest req, HttpServletResponse resp) throws Exception
    {
        logger_.info("start processing...");
        String back = server_.processRequest(req, resp);
        if (back != null)
        {
            logger_.info("processing not null :" + back);
            PrintWriter out = resp.getWriter();
            if (back.startsWith("http"))
            {
                resp.setContentType("text/html");
                out.write("<html><body><a href='" + back + "' id='login'>Login</a></body></html>");
            }
            else
            {
                out.write(back);
            }
        }
    }
}
