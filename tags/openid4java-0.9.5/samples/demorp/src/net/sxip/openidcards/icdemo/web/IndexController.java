package net.sxip.openidcards.icdemo.web;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.*;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.infocard.InfocardException;
import org.openid4java.OpenIDException;
import org.openid4java.infocard.OpenIDToken;

/**
 * @author Marius Scurtescu
 */
public class IndexController extends AbstractController
{
    private static Logger _logger = Logger.getLogger(IndexController.class);

    private static final String LOGGEDIN ="loggedin";

    // attribute typeUri -> "nice label / alias"
    private static HashMap<String,String> _attributes = new HashMap<String,String>();

    static
    {
        _attributes.put("http://axschema.org/contact/email", "Email");
        _attributes.put("http://axschema.org/namePerson/first", "FirstName");
        _attributes.put("http://axschema.org/namePerson/last", "LastName");
        _attributes.put("http://axschema.org/contact/phone/default", "Phone");
        _attributes.put("http://axschema.org/contact/postalAddress/home", "Address");
        _attributes.put("http://axschema.org/contact/city/home", "City");
        _attributes.put("http://axschema.org/contact/postalCode/home", "ZipCode");
        _attributes.put("http://axschema.org/contact/country/home", "Country");
        _attributes.put("http://axschema.org/contact/web/blog", "Blog");
    }

    private String _loginView;
    private String _homeView;
    private String _postView;
    private String _errorView;

    private String _stsUrl;
    private String _axUrl;

    private ConsumerManager _consumerManager;

    private String _baseUrl;

    public void setConsumerManager(ConsumerManager consumerManager)
    {
        this._consumerManager = consumerManager;
    }

    public void setLoginView(String loginView)
    {
        _loginView = loginView;
    }

    public void setHomeView(String homeView)
    {
        _homeView = homeView;
    }

    public void setPostView(String postView)
    {
        _postView = postView;
    }


    public void setErrorView(String _errorView)
    {
        this._errorView = _errorView;
    }

    public void setStsUrl(String stsUrl)
    {
        this._stsUrl = stsUrl;
    }

    public void setAxUrl(String axUrl)
    {
        this._axUrl = axUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        _baseUrl = baseUrl;
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response)
    {
        HttpSession session = request.getSession();
        String return_to = _baseUrl + request.getContextPath() + "/";
        String mode = request.getParameter("openid.mode");
        String xmlToken = request.getParameter("xmlToken");
        String openIdIdentifier = request.getParameter("openid_identifier");

        try
        {
        if (mode != null)
        {
            // received a standard OpenID response
            ParameterList openidResp = extractFromOpenIDPost(request);
            return processOpenIDResp(request, session, openidResp);
        }
        else if (xmlToken != null)
        {
            // received an xmlToken from an identity selector
            ParameterList openidResp = extractFromInfocardPost(request);
            return processOpenIDResp(request, session, openidResp);
        }
        else if (openIdIdentifier != null)
        {
            return buildFetchReq(openIdIdentifier, session, return_to);
        }
        else
        {
            if (request.getParameter("logout") != null)
            {
                _logger.info("Logging out...");

                session.removeAttribute(LOGGEDIN);
                session.removeAttribute("message");
            }

            if (session.getAttribute(LOGGEDIN) == null)
            {
                _logger.info("Showing login page...");

                return showLoginPage();
            }
            else
            {
                _logger.info("(Re)Showing home / data view...");

                return new ModelAndView(_homeView);
            }
        }
        }
        catch (Exception e)
        {
            _logger.error("Error encountered: ", e);

            return new ModelAndView(_errorView);
        }
    }

    private ModelAndView showLoginPage()
    {
        Map<String, String> model = new HashMap<String, String>();

        model.put("title", "OpenID Infocards Demo");

        String loginMessage1 =
            "This is a demonstration of using OpenID Information Cards" +
            "<sup>*</sup> to log into an OpenID Relying Party.<br /><br />\n" +
            "Besides OpenID Authentication, the Relying Party " +
            "also requests profile data using Information Card conventions, " +
            "and the values are returned in the OpenID assertion, " +
            "using the OpenID Attribute Exchange<sup>**</sup> extension.\n";

        String loginMessage2 =
            "<sup>*</sup> You can get an OpenID Information Card " +
            "<a href=\"" + _stsUrl + "\">here</a>." +
            "The OpenID Information Cards spec can be found " +
            "<a href=\"TBD\">here</a>." +
            "<br /><sup>**</sup> The OpenID Attribute Exchange spec " +
            "can be found <a href=\"" + _axUrl + "\">here</a>\"";

        model.put("contents", loginMessage1);
        model.put("contents2", loginMessage2);

        return new ModelAndView(_loginView, model);
    }

    private ModelAndView buildFetchReq(String identifier, HttpSession session, String return_to)
        throws OpenIDException
    {
        _logger.info("Building auth + fetch request for: " + identifier);

        Map<String,Object> model = new HashMap<String,Object>();
        List discoveries;
        String errorMsg = "";

        try
        {
            discoveries = _consumerManager.discover(identifier);
        }
        catch (DiscoveryException e)
        {
            _logger.error("Error while performing HTML discovery on "
                          + identifier, e);
            discoveries = null;
            errorMsg = "<br /><br /><em>" + e.getMessage() + "</em>";
        }

        if (discoveries == null || discoveries.size() == 0)
        {
            _logger.error("Discovery failed on: " + identifier);
            
            model.put("message", "The " + identifier + " identifier could not be resolved." + errorMsg);

            return new ModelAndView(_loginView, model);
        }

        DiscoveryInformation discovered = _consumerManager.associate(discoveries);

        // store the discovery information in the session for later use
        session.setAttribute("discovered", discovered);

        FetchRequest fetch = FetchRequest.createFetchRequest();

        for (String typeUri : _attributes.keySet())
        {
            fetch.addAttribute(_attributes.get(typeUri), typeUri, false);
        }

        AuthRequest req = _consumerManager.authenticate(discovered, return_to);
        req.addExtension(fetch);

        model.put("message", req);

        _logger.info("Sending fetch request / auto-post view...");

        return new ModelAndView(_postView, model);
    }

    private ParameterList extractFromInfocardPost(HttpServletRequest request)
        throws InfocardException
    {
        _logger.info("Extracting OpenID AuthResponse / Fetch Response from Infocard POST..." );

        String xmlToken = request.getParameter("xmlToken");

        request.getSession().setAttribute("openidAssertion", xmlToken);

        OpenIDToken token = OpenIDToken.createFromXmlToken(xmlToken);

        return token.getOpenIDParams();
    }

    private ParameterList extractFromOpenIDPost(HttpServletRequest request)
        throws MessageException
    {
        _logger.info("Extracting OpenID AuthResponse / Fetch Response from OpenID POST..." );

        ParameterList openidAssertion = new ParameterList(request.getParameterMap());

        request.getSession().setAttribute("openidAssertion", openidAssertion.toString());

        return openidAssertion;
    }

    private ModelAndView processOpenIDResp(HttpServletRequest request,
                                           HttpSession session,
                                           ParameterList openidResp)
        throws OpenIDException
    {
        _logger.info("Processing OpenID auth / fetch response..." );

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("title", "OpenID 2.0 OpenID InfoCards Demo");

        // retrieve the previously stored discovery information
        DiscoveryInformation discovered = (DiscoveryInformation) session.getAttribute("discovered");

        StringBuffer receivingURL = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString != null && queryString.length() > 0)
            receivingURL.append("?").append(request.getQueryString());

        // verify the response
        VerificationResult verification = _consumerManager.verify(
                receivingURL.toString(), openidResp, discovered);

        verification.getVerifiedId();

        Message authResponse = verification.getAuthResponse();

        if (!(authResponse instanceof AuthSuccess))
        {
            _logger.error("Negative auth response received; showing login view...");

            model.put("message", "Negative authentication response received from the OpenID Provider.");

            return new ModelAndView(_loginView, model);
        }

        Identifier verified = verification.getVerifiedId();
        String identifier;

        if (verified == null)
        {
            _logger.error("OpenID verification failed; showing login view...");

            model.put("message", verification.getStatusMsg());

            return new ModelAndView(_loginView, model);
        }
        else
        {
            identifier = verified.getIdentifier();
        }

        AuthSuccess authSuccess = (AuthSuccess) authResponse;

        FetchResponse fetchResp = null;

        Map<String,String> attributes = new LinkedHashMap<String,String>();

        MessageExtension ext;
        if ( authSuccess.hasExtension(AxMessage.OPENID_NS_AX) &&
             (ext = authSuccess.getExtension(AxMessage.OPENID_NS_AX)) instanceof FetchResponse)
        {
            fetchResp = (FetchResponse) ext;

            // extract the rest of the optional attributes
            List aliases = fetchResp.getAttributeAliases();
            Map types = fetchResp.getAttributeTypes();
            String alias;
            List values;
            for (Object a : aliases)
            {
                alias = (String) a;
                values = fetchResp.getAttributeValues(alias);
                attributes.put(_attributes.get(types.get(alias)),
                    values.size() > 0 ? (String) values.get(0) : null);
            }
        }

        session.setAttribute(LOGGEDIN, "");

        session.setAttribute("attributes", attributes);

        session.setAttribute("identifier", identifier);
        session.setAttribute("message", fetchResp);

        _logger.info("AX success; showing home / data view...");

        return new ModelAndView(_homeView, model);
    }
}
