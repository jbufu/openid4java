/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.samples;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.consumer.SampleConsumer;

public class LoginServlet extends HttpServletSupport
{
    private static final long serialVersionUID = 1L;
    private SampleConsumer consumer_;

    public LoginServlet(SampleConsumer consumer)
    {
        consumer_ = consumer;
    }

    protected void onService(HttpServletRequest req, HttpServletResponse resp) throws Exception
    {
        if (req.getParameter("openid_identifier") != null)
        {
            logger_.info("openind_identifier set => try to consume");
            consumer_.authRequest(req.getParameter("openid_identifier"), req, resp);
        }
        else
        {
            logger_.info("display form");
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println("<html><body><form><input type='text' name='openid_identifier'/><input type='submit'/></form></body></html>");
        }
    }

}
