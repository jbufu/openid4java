# Attribute Exchange HowTo #


## Relying Party sends a FetchRequest ##

```
FetchRequest fetch = FetchRequest.createFetchRequest();

fetch.addAttribute("FirstName", "http://schema.openid.net/namePerson/first", true);
fetch.addAttribute("LastName", "http://schema.openid.net/namePerson/last", true);
fetch.addAttribute("Email", "http://schema.openid.net/contact/email", true);

// wants up to three email addresses
fetch.setCount("Email", 3);

AuthRequest req = _consumerManager.authenticate(discovered, return_to);
req.addExtension(fetch);
```

## OpenID Provider receives a FetchRequest ##
```
if (authReq.hasExtension(AxMessage.OPENID_NS_AX))
{
    MessageExtension ext = authReq.getExtension(AxMessage.OPENID_NS_AX)

    if (ext instanceof FetchRequest)
    {
        FetchRequest fetchReq = (FetchRequest) ext;
        Map required = fetchReq.getAttributes(true);
        Map optional = fetchReq.getAttributes(false);
        // prompt the user
    }
    else if (ext instanceof StoreRequest)
    {
        ...
    }
}
```

## OpenID Provider sends a FetchResponse ##
```
    // data released by the user
    Map userData = new HashMap();
    //userData.put("Email", "user@example.com");

    FetchResponse fetchResp = FetchResponse.createFetchResponse(fetchReq, userData);

    // (alternatively) manually add attribute values
    fetchResp.addAttribute("email", "http://schema.openid.net/contact/email", "user@example.com");

    authSuccess.addExtension(fetchResp);
```

## Relying Party receives a FetchResponse ##
```
if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX))
{
    MessageExtension ext = authSuccess.getExtension(AxMessage.OPENID_NS_AX);

    if (ext instanceof FetchResponse)
    {
        FetchResponse fetchResp = (FetchResponse) ext;
        
        String firstName = fetcResp.getAttributeValue("FirstName");
        String lastName = fetchResp.getAttributeValue("LastName");
        
        // can have multiple values
        List emails = fetchResp.getAttributeValues("Email");
    }
    else if (ext instanceof StoreResponse)
    {
        ...
    }
}
```