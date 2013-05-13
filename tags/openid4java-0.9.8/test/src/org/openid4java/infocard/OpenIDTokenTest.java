/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.infocard;

import junit.framework.TestCase;

public class OpenIDTokenTest extends TestCase
{

    public OpenIDTokenTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
    }

    public void testCreateFromXmlToken() throws Exception
    {
        String xmlToken ="<openid:OpenIDToken xmlns:openid=\"http://specs.openid.net/auth/2.0\">" +
            "openid.ns:http://specs.openid.net/auth/2.0\n" +
            "\n" +
            "openid.mode:id_res\n" +
            "\n" +
            "openid.op_endpoint:https://example-op.com/openid-server/\n" +
            "\n" +
            "openid.claimed_id:https://example-op.com/johndoe/\n" +
            "\n" +
            "openid.identity:https://example-op.com/johndoe/\n" +
            "\n" +
            "openid.return_to:https://example-rp.com/openid-infocard-endpoint/\n" +
            "\n" +
            "openid.response_nonce:2007-06-28T22:16:58Z0\n" +
            "\n" +
            "openid.assoc_handle:d38f38e8166443cb\n" +
            "\n" +
            "openid.signed:op_endpoint,claimed_id,identity,return_to,response_nonce,assoc_handle\n" +
            "\n" +
            "openid.sig:PZNucb3/5KnEHsOXEMFkg1FJAnGD+UbGR1LqsscVvEc=\n" +
            "\n" +
            "openid.ns.ext1:http://openid.net/srv/ax/1.0\n" +
            "\n" +
            "openid.ext1.mode:fetch_response\n" +
            "\n" +
            "openid.ext1.type.FirstName:http://axschema.org/namePerson/first\n" +
            "\n" +
            "openid.ext1.value.FirstName:John\n" +
            "\n" +
            "openid.ext1.type.LastName:http://axschema.org/namePerson/last\n" +
            "\n" +
            "openid.ext1.value.LastName:Doe\n" +
            "\n" +
            "openid.ext1.type.email:http://axschema.org/contact/email\n" +
            "\n" +
            "openid.ext1.value.email:johndoe@example.com" +
            "</openid:OpenIDToken>";

        OpenIDToken token = OpenIDToken.createFromXmlToken(xmlToken);

        assertNotNull(token);

    }
}
