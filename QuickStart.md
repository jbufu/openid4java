# Quick Start #

Once you have [installed](Installation.md) the library and have obtained the user's OpenID identifier, you have to put in the following code to have your webapp perform authentication using OpenID:

## Instantiate a ConsumerManager Object ##
```
    public ConsumerManager manager;

    public SampleConsumer() throws ConsumerException
    {
        _manager = new ConsumerManager();
    }
```

The ConsumerManager will do all the OpenID hard work for you.

## Define a ReturnURL ##

This is the endpoint where your webapp will receive and process the authentication responses from the OpenID Provider.

```
    String _returnURL = "http://example.com/openid";
```

## Create an Authentication Request ##

```
    // perform discovery on the user-supplied identifier
    List discoveries = manager.discover(userSuppliedString);

    // attempt to associate with the OpenID provider
    // and retrieve one service endpoint for authentication
    DiscoveryInformation discovered = manager.associate(discoveries);

    // store the discovery information in the user's session for later use
    // leave out for stateless operation / if there is no session
    session.setAttribute("discovered", discovered);

    // obtain a AuthRequest message to be sent to the OpenID provider
    AuthRequest authReq = manager.authenticate(discovered, _returnURL);
```

## Redirect the User to Their OpenID Provider ##
```
    httpResp.sendRedirect(authReq.getDestinationUrl(true));
```

## Verify the OpenID Provider's Authentication Response ##

Receive the response at your webapp's ReturnURL and process it like this:
```
    // extract the parameters from the authentication response
    // (which comes in as a HTTP request from the OpenID provider)
    ParameterList openidResp = new ParameterList(request.getParameterMap());

    // retrieve the previously stored discovery information
    DiscoveryInformation discovered = (DiscoveryInformation) session.getAttribute("discovered");

    // extract the receiving URL from the HTTP request
    StringBuffer receivingURL = request.getRequestURL();
    String queryString = request.getQueryString();
    if (queryString != null && queryString.length() > 0)
        receivingURL.append("?").append(request.getQueryString());

    // verify the response
    VerificationResult verification = _consumerManager.verify(receivingURL.toString(), openidResp, discovered);

    // examine the verification result and extract the verified identifier
    Identifier verified = verification.getVerifiedId();

    if (verified != null)
        // success, use the verified identifier to identify the user
    else
        // OpenID authentication failed
```

## Where to go next? ##

You can see all the above put together in the SampleConsumer class.