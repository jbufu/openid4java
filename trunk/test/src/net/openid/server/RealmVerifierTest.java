/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.server;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Element;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class RealmVerifierTest extends TestCase
{
    private static final String TEST_DATA_FILE = "net/openid/server/RealmTestData.xml";
    private static final Map _resultCodes = new HashMap();

    static
    {
        _resultCodes.put("OK", new Integer(RealmVerifier.OK));
        _resultCodes.put("DENIED_REALM", new Integer(RealmVerifier.DENIED_REALM));
        _resultCodes.put("MALFORMED_REALM", new Integer(RealmVerifier.MALFORMED_REALM));
        _resultCodes.put("MALFORMED_RETURN_TO_URL", new Integer(RealmVerifier.MALFORMED_RETURN_TO_URL));
        _resultCodes.put("FRAGMENT_NOT_ALLOWED", new Integer(RealmVerifier.FRAGMENT_NOT_ALLOWED));
        _resultCodes.put("PROTOCOL_MISMATCH", new Integer(RealmVerifier.PROTOCOL_MISMATCH));
        _resultCodes.put("PORT_MISMATCH", new Integer(RealmVerifier.PORT_MISMATCH));
        _resultCodes.put("PATH_MISMATCH", new Integer(RealmVerifier.PATH_MISMATCH));
        _resultCodes.put("DOMAIN_MISMATCH", new Integer(RealmVerifier.DOMAIN_MISMATCH));
    }

    private RealmVerifier _realmVerifier;

    public RealmVerifierTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        _realmVerifier = new RealmVerifier();
    }

    public void testXmlFile() throws IOException, JDOMException
    {
        InputStream in = getClass().getClassLoader().getResourceAsStream(TEST_DATA_FILE);

        assertNotNull("XML data file could not be loaded: " + TEST_DATA_FILE, in);

        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(in);
        Element testSuite = document.getRootElement();
        List tests = testSuite.getChildren("test");
        for (int i = 0; i < tests.size(); i++)
        {
            Element test = (Element) tests.get(i);

            String result = test.getAttributeValue("result");
            String realm = test.getAttributeValue("realm");
            String returnTo = test.getAttributeValue("returnTo");
            String message = test.getAttributeValue("message");

            Integer resultCode = (Integer) _resultCodes.get(result);

            if (message == null)
                assertEquals(resultCode.intValue(), _realmVerifier.match(realm, returnTo));
            else
                assertEquals(message, resultCode.intValue(), _realmVerifier.match(realm, returnTo));
        }
    }

    public static Test suite()
    {
        return new TestSuite(RealmVerifierTest.class);
    }
}
