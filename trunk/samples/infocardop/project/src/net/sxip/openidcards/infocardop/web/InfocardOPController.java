package net.sxip.openidcards.infocardop.web;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

import java.io.IOException;

import org.openid4java.server.ServerManager;
import org.openid4java.server.ServerException;
import org.openid4java.message.*;
import org.openid4java.OpenIDException;

public class InfocardOPController extends AbstractController
{
    private static Logger _log = Logger.getLogger(InfocardOPController.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private String _openidErrorView;

    private ServerManager _manager;

    public void setOpenidErrorView(String openidErrorView)
    {
        this._openidErrorView = openidErrorView;
    }

    public void setServerManager(ServerManager manger)
    {
        this._manager = manger;
    }

    public ServerManager getManager()
    {
        return _manager;
    }

    protected ModelAndView handleRequestInternal(
            HttpServletRequest httpReq,
            HttpServletResponse httpResp)
    {
        if ("GET".equals(httpReq.getMethod()))
            return new ModelAndView(_openidErrorView);

        // extract the parameters from the requestParams
        ParameterList requestParams = new ParameterList(httpReq.getParameterMap());

        String mode = requestParams.getParameterValue("openid.mode");
        boolean compat = ! requestParams.hasParameter("openid.ns");

        try
        {
            if ("check_authentication".equals(mode))
                return handleVerifyReq(httpReq, httpResp, requestParams);

            else
                return handleUnknownReq(httpReq, httpResp);
        }
        catch (OpenIDException e)
        {
            _log.error("Error handling OpenID request: ", e);

            return directError(httpResp, e.getMessage(), compat);
        }
    }


    private ModelAndView handleVerifyReq(HttpServletRequest httpReq,
                                         HttpServletResponse httpResp,
                                         ParameterList requestParams)
        throws ServerException
    {
        // --- processing a verification requestParams ---
        Message response = _manager.verify(requestParams);
        String responseText = response.keyValueFormEncoding();

        _log.info("Processed direct verification request from: "
                  + httpReq.getRemoteAddr());

        return directResponse(httpResp, responseText);
    }

    private ModelAndView handleUnknownReq(HttpServletRequest httpReq,
                                          HttpServletResponse httpResp)
        throws ServerException
    {
        // --- error response ---
        Message response = DirectError.createDirectError("Unknown requestParams");
        String responseText = response.keyValueFormEncoding();

        _log.error("Sending direct error response to "
                   + httpReq.getRemoteAddr());

        return directResponse(httpResp, responseText);
    }


    private ModelAndView directResponse(HttpServletResponse httpResp, String response)
        throws ServerException
    {
        if (DEBUG) _log.debug("Sending direct response:\n" + response);

        try
        {
            ServletOutputStream os = httpResp.getOutputStream();
            os.write(response.getBytes());
            os.close();
        }
        catch (IOException e)
        {
            throw new ServerException("Error generating direct verification response", e);
        }

        return null;
    }

    private ModelAndView directError(HttpServletResponse httpResp,
                                     String response, boolean compat)
    {
        if (DEBUG) _log.debug("Sending direct response:\n" + response);

        try
        {
            DirectError err = DirectError.createDirectError(response, compat);

            ServletOutputStream os = httpResp.getOutputStream();
            os.write(err.keyValueFormEncoding().getBytes());
            os.close();
        }
        catch (IOException e)
        {
            _log.error("Error generating direct error response", e);
        }

        return null;
    }
}
