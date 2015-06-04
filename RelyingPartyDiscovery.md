# Relying Parties #

Relying Parties must publish their endpoints in order for the OpenID
Providers to be able to verify authentication requests and prevent
proxy attacks. The Yadis protocol and realm verification mechanisms
are used for this purpose. See the section "Discovering OpenID
Relying Parties" of the OpenID Authentication specification for
details.

Example:
```
<Service xmlns="xri://$xrd*($v*2.0)">
  <Type>http://specs.openid.net/auth/2.0/return_to</Type>
  <URI>http://consumer.example.com/return</URI>
</Service>
```
The RP should publish the above 

&lt;Service&gt;

 element at their realm URL.
All OpenID Authentication request sent by this RP should contain
openid.return\_to values matching the http://consumer.example.com/return
realm.

# OpenID Providers #

Validation of openid.return\_to values against Relying Party Discovery
endpoints is enabled by default. This feature can be disabled with
ServerManager.setEnforceRpId(false).