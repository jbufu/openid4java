/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.samples;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.consumer.SampleConsumer;

public class LoginCallbackServlet extends HttpServletSupport
{
    private static final long serialVersionUID = 1L;
    private SampleConsumer consumer_;

    public LoginCallbackServlet(SampleConsumer consumer)
    {
        consumer_ = consumer;
    }

    protected void onService(HttpServletRequest req, HttpServletResponse resp) throws Exception
    {
        if (req.getParameterMap().isEmpty())
        {
            logger_.info("no parameter => display required <head> (with only a title tag)");
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println("<html><head><title>login callback</title></head></html>");
        }
        else
        {
            logger_.info("verify response");
            resp.setContentType("text/plain");
            PrintWriter out = resp.getWriter();
            if (consumer_.verifyResponse(req) != null)
            {
                logger_.info("success");
                out.print("success{");
                out.print("openid_identifier:" + req.getSession().getAttribute("openid_identifier"));
                out.print("emailFromFetch:" + req.getSession().getAttribute("emailFromFetch"));
                out.print(", emailFromSReg:" + req.getSession().getAttribute("emailFromSReg"));
                out.print("}");
            }
            else
            {
                logger_.info("failure");
                out.println("failed");
            }
            out.flush();
        }
    }

}
