package org.openid4java.samples;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

public abstract class HttpServletSupport extends HttpServlet
{
    protected static final long serialVersionUID = 1L;
    protected static Exception lastException;

    protected static int count_;
    protected Logger logger_;

    public HttpServletSupport()
    {
        logger_ = Logger.getLogger(this.getClass());
    }

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        count_++;
        NDC.push(this.getClass().getSimpleName() + "/" + count_);
        logger_.info("begin onService");
        try
        {
            onService(req, resp);
        }
        catch (Exception exc)
        {
            lastException = exc;
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally
        {
            logger_.info("end onService");
            NDC.pop();
        }
    }

    protected abstract void onService(HttpServletRequest req, HttpServletResponse resp) throws Exception;
}
