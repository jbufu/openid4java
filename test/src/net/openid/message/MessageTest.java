/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.message;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.net.URLEncoder;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class MessageTest extends TestCase
{
    private Message _msg;

    public MessageTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        ParameterList params = new ParameterList();

        params.set(new Parameter("key1", "value1"));
        params.set(new Parameter("key1", "value2"));
        params.set(new Parameter("key2", "value1"));

        _msg = new Message(params);
    }

    public void tearDown() throws Exception
    {
        _msg = null;
    }

    public void testKeyValueFormEncoding() throws Exception
    {
        String keyValueForm = "key1:value1\nkey1:value2\nkey2:value1\n";

        assertEquals(URLEncoder.encode(keyValueForm, "UTF-8"),
                _msg.keyValueFormEncoding());
    }

    public void testWwwFormEncoding() throws Exception
    {
        String wwwForm = "openid.key1=value1&openid.key1=value2&openid.key2=value1";

        assertEquals(wwwForm, _msg.wwwFormEncoding());
    }

    public static Test suite()
    {
        return new TestSuite(MessageTest.class);
    }

}
