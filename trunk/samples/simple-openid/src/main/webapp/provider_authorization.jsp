<%@ page session="true" %>
<%@ page import="java.util.List, org.openid4java.message.AuthSuccess, org.openid4java.server.InMemoryServerAssociationStore, org.openid4java.message.DirectError,org.openid4java.message.Message,org.openid4java.message.ParameterList, org.openid4java.discovery.Identifier, org.openid4java.discovery.DiscoveryInformation, org.openid4java.message.ax.FetchRequest, org.openid4java.message.ax.FetchResponse, org.openid4java.message.ax.AxMessage,  org.openid4java.message.*, org.openid4java.OpenIDException, java.util.List, java.io.IOException, javax.servlet.http.HttpSession, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.openid4java.server.ServerManager, org.openid4java.consumer.InMemoryConsumerAssociationStore, org.openid4java.consumer.VerificationResult" %>

<%
    // HOWTO:
    // the session var parameterlist contains openid authreq message parameters
    // this JSP should set the session attribute var "authenticatedAndApproved" and
    // redirect to provider.jsp?_action=complete

    ParameterList requestp=(ParameterList) session.getAttribute("parameterlist");
    String openidrealm=requestp.hasParameter("openid.realm") ? requestp.getParameterValue("openid.realm") : null;
    String openidreturnto=requestp.hasParameter("openid.return_to") ? requestp.getParameterValue("openid.return_to") : null;
    String openidclaimedid=requestp.hasParameter("openid.claimed_id") ? requestp.getParameterValue("openid.claimed_id") : null;
    String openididentity=requestp.hasParameter("openid.identity") ? requestp.getParameterValue("openid.identity") : null;

%>
<h1>Provider Authentication and Authorization</h1>
<em>Right now, this doesn't provide a fancy interface - authenticate the user (not done, do whatever authn you want),
do some presentation about whats being asked of the user,
and then go back to the provider.jsp.<p>
This JSP just asks you to click a link without authentication.</em><p>
<%
    if (request.getParameter("action") == null)
    {
        String site=(String) (openidrealm == null ? openidreturnto : openidrealm);

%>
    <strong>ClaimedID:</strong> <pre><%= openidclaimedid%></pre><br>
    <strong>Identity:</strong> <pre><%= openididentity %> </pre><br>
    <strong>Site:</strong> <pre> <%= site %></pre><br>
    Click <a href="?action=authorize" id="login">To become logged in and authorize</a>
<%
    }
    else // Logged in
    {
        session.setAttribute("authenticatedAndApproved", Boolean.TRUE); // No need to change openid.* session vars
        response.sendRedirect("provider.jsp?_action=complete");
    }
%>
