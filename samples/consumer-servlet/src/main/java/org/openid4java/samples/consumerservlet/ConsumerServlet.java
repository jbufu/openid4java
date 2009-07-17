/**
 * Created on 2007-4-14 00:54:50
 */
package org.openid4java.samples.consumerservlet;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;
import org.openid4java.association.AssociationSessionType;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;
import org.openid4java.util.HttpClientFactory;
import org.openid4java.util.ProxyProperties;

/**
 * @author Sutra Zhou
 */
public class ConsumerServlet extends javax.servlet.http.HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5998885243419513055L;
	private static final String OPTIONAL_VALUE = "0";
	private static final String REQUIRED_VALUE = "1";
	private static final Log LOG = LogFactory.getLog(ConsumerServlet.class);

	private ServletContext context;
	private ConsumerManager manager;

	/**
	 * {@inheritDoc}
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		context = config.getServletContext();

		LOG.debug("context: " + context);

		try {
			// --- Forward proxy setup (only if needed) ---
			ProxyProperties proxyProps = getProxyProperties(config);
			if (proxyProps != null) {
				LOG.debug("ProxyProperties: " + proxyProps);
				HttpClientFactory.setProxyProperties(proxyProps);
			}

			this.manager = new ConsumerManager();
			manager.setAssociations(new InMemoryConsumerAssociationStore());
			manager.setNonceVerifier(new InMemoryNonceVerifier(5000));
			manager.setMinAssocSessEnc(AssociationSessionType.DH_SHA256);
		} catch (ConsumerException e) {
			throw new ServletException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if ("true".equals(req.getParameter("is_return"))) {
			processReturn(req, resp);
		} else {
			String identifier = req.getParameter("openid_identifier");
			if (identifier != null) {
				this.authRequest(identifier, req, resp);
			} else {
				this.getServletContext().getRequestDispatcher("/index.jsp")
						.forward(req, resp);
			}
		}
	}

	private void processReturn(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Identifier identifier = this.verifyResponse(req);
		LOG.debug("identifier: " + identifier);
		if (identifier == null) {
			this.getServletContext().getRequestDispatcher("/index.jsp")
					.forward(req, resp);
		} else {
			req.setAttribute("identifier", identifier.getIdentifier());
			this.getServletContext().getRequestDispatcher("/return.jsp")
					.forward(req, resp);
		}
	}

	// --- placing the authentication request ---
	public String authRequest(String userSuppliedString,
			HttpServletRequest httpReq, HttpServletResponse httpResp)
			throws IOException, ServletException {
		try {
			// configure the return_to URL where your application will receive
			// the authentication responses from the OpenID provider
			// String returnToUrl = "http://example.com/openid";
			String returnToUrl = httpReq.getRequestURL().toString()
					+ "?is_return=true";

			// perform discovery on the user-supplied identifier
			List discoveries = manager.discover(userSuppliedString);

			// attempt to associate with the OpenID provider
			// and retrieve one service endpoint for authentication
			DiscoveryInformation discovered = manager.associate(discoveries);

			// store the discovery information in the user's session
			httpReq.getSession().setAttribute("openid-disc", discovered);

			// obtain a AuthRequest message to be sent to the OpenID provider
			AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

			// Attribute Exchange example: fetching the 'email' attribute
			// FetchRequest fetch = FetchRequest.createFetchRequest();
			SRegRequest sregReq = SRegRequest.createFetchRequest();

			String[] attributes = { "nickname", "email", "fullname", "dob",
					"gender", "postcode", "country", "language", "timezone" };
			for (int i = 0, l = attributes.length; i < l; i++) {
				String attribute = attributes[i];
				String value = httpReq.getParameter(attribute);
				if (OPTIONAL_VALUE.equals(value)) {
					sregReq.addAttribute(attribute, false);
				} else if (REQUIRED_VALUE.equals(value)) {
					sregReq.addAttribute(attribute, true);
				}
			}

			// attach the extension to the authentication request
			if (!sregReq.getAttributes().isEmpty()) {
				authReq.addExtension(sregReq);
			}

			if (!discovered.isVersion2()) {
				// Option 1: GET HTTP-redirect to the OpenID Provider endpoint
				// The only method supported in OpenID 1.x
				// redirect-URL usually limited ~2048 bytes
				httpResp.sendRedirect(authReq.getDestinationUrl(true));
				return null;
			} else {
				// Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)

				RequestDispatcher dispatcher = getServletContext()
						.getRequestDispatcher("/formredirection.jsp");
				httpReq.setAttribute("prameterMap", httpReq.getParameterMap());
				httpReq.setAttribute("message", authReq);
				// httpReq.setAttribute("destinationUrl", httpResp
				// .getDestinationUrl(false));
				dispatcher.forward(httpReq, httpResp);
			}
		} catch (OpenIDException e) {
			// present error to the user
			throw new ServletException(e);
		}

		return null;
	}

	// --- processing the authentication response ---
	public Identifier verifyResponse(HttpServletRequest httpReq)
			throws ServletException {
		try {
			// extract the parameters from the authentication response
			// (which comes in as a HTTP request from the OpenID provider)
			ParameterList response = new ParameterList(httpReq
					.getParameterMap());

			// retrieve the previously stored discovery information
			DiscoveryInformation discovered = (DiscoveryInformation) httpReq
					.getSession().getAttribute("openid-disc");

			// extract the receiving URL from the HTTP request
			StringBuffer receivingURL = httpReq.getRequestURL();
			String queryString = httpReq.getQueryString();
			if (queryString != null && queryString.length() > 0)
				receivingURL.append("?").append(httpReq.getQueryString());

			// verify the response; ConsumerManager needs to be the same
			// (static) instance used to place the authentication request
			VerificationResult verification = manager.verify(receivingURL
					.toString(), response, discovered);

			// examine the verification result and extract the verified
			// identifier
			Identifier verified = verification.getVerifiedId();
			if (verified != null) {
				AuthSuccess authSuccess = (AuthSuccess) verification
						.getAuthResponse();

				if (authSuccess.hasExtension(SRegMessage.OPENID_NS_SREG)) {
					MessageExtension ext = authSuccess
							.getExtension(SRegMessage.OPENID_NS_SREG);
					if (ext instanceof SRegResponse) {
						SRegResponse sregResp = (SRegResponse) ext;
						for (Iterator iter = sregResp.getAttributeNames()
								.iterator(); iter.hasNext();) {
							String name = (String) iter.next();
							String value = sregResp.getParameterValue(name);
							httpReq.setAttribute(name, value);
						}
					}
				}
				if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
					FetchResponse fetchResp = (FetchResponse) authSuccess
							.getExtension(AxMessage.OPENID_NS_AX);

					// List emails = fetchResp.getAttributeValues("email");
					// String email = (String) emails.get(0);

					List aliases = fetchResp.getAttributeAliases();
					for (Iterator iter = aliases.iterator(); iter.hasNext();) {
						String alias = (String) iter.next();
						List values = fetchResp.getAttributeValues(alias);
						if (values.size() > 0) {
							LOG.debug(alias + " : " + values.get(0));
							httpReq.setAttribute(alias, values.get(0));
						}
					}
				}

				return verified; // success
			}
		} catch (OpenIDException e) {
			// present error to the user
			throw new ServletException(e);
		}

		return null;
	}

	/**
	 * Get proxy properties from the context init params.
	 * 
	 * @return proxy properties
	 */
	private static ProxyProperties getProxyProperties(ServletConfig config) {
		ProxyProperties proxyProps;
		String host = config.getInitParameter("proxy.host");
		LOG.debug("proxy.host: " + host);
		if (host == null) {
			proxyProps = null;
		} else {
			proxyProps = new ProxyProperties();
			String port = config.getInitParameter("proxy.port");
			String username = config.getInitParameter("proxy.username");
			String password = config.getInitParameter("proxy.password");
			String domain = config.getInitParameter("proxy.domain");
			proxyProps.setProxyHostName(host);
			proxyProps.setProxyPort(Integer.parseInt(port));
			proxyProps.setUserName(username);
			proxyProps.setPassword(password);
			proxyProps.setDomain(domain);
		}
		return proxyProps;
	}
}
