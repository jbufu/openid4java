# Simple Registration HowTo #

The usage pattern exemplified below is almost identical to the usage of [Attribute Exchange](AttributeExchangeHowTo.md).

Attribute Exchange can transfer any type of attributes. For more information see the [OpenID Attribute Exchange 1.0 specification](http://openid.net/specs/openid-attribute-exchange-1_0-04.html) and [OpenID Attribute Types](http://openid.net/specs/openid-attribute-types-1_0-02.html).

## Relying Party sends a SRegRequest ##

```
SRegRequest sregReq = SRegRequest.createFetchRequest();

sregReq.addAttribute("fullname", true);
sregReq.addAttribute("nickname", true);
sregReq.addAttribute("email", true);

AuthRequest req = _consumerManager.authenticate(discovered, return_to);
req.addExtension(sregReq);
```

## OpenID Provider receives a SRegRequest ##
```
if (authReq.hasExtension(SRegMessage.OPENID_NS_SREG))
{
    MessageExtension ext = authReq.getExtension(SRegMessage.OPENID_NS_SREG)

    if (ext instanceof SRegRequest)
    {
        SRegRequest sregReq = (SRegRequest) ext;
        List required = sregReq.getAttributes(true);
        List optional = sregReq.getAttributes(false);
        // prompt the user
    }
}
```

## OpenID Provider sends a SRegResponse ##
```
    // data released by the user
    Map userData = new HashMap();
    //userData.put("email", "user@example.com");

    SRegResponse sregResp = SRegResponse.createSRegResponse(sregReq, userData);

    // (alternatively) manually add attribute values
    sregResp.addAttribute("email", "user@example.com");

    authSuccess.addExtension(sregResp);
    serverManager.sign(authSuccess);
```

## Relying Party receives a SRegResponse ##
```
if (authSuccess.hasExtension(SRegMessage.OPENID_NS_SREG))
{
    MessageExtension ext = authSuccess.getExtension(SRegMessage.OPENID_NS_SREG);

    if (ext instanceof SRegResponse)
    {
        SRegResponse sregResp = (SRegResponse) ext;
        
        String fullName = sregResp.getAttributeValue("fullname");
        String nickName = sregResp.getAttributeValue("nickname");
        String email = sregResp.getAttributeValues("email");
    }
}
```