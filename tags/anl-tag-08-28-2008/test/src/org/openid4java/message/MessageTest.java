/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;

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
        String keyValueForm = "key1:value2\nkey2:value1\n";

        assertEquals(keyValueForm, _msg.keyValueFormEncoding());
    }

    public void testWwwFormEncoding() throws Exception
    {
        String wwwForm = "openid.key1=value2&openid.key2=value1";

        assertEquals(wwwForm, _msg.wwwFormEncoding());
    }

    public static Test suite()
    {
        return new TestSuite(MessageTest.class);
    }

    public void testNotAllowedChars() throws Exception
    {
        Parameter param;
        Map parameterMap;

        try
        {
            // semicolon in key
            param = new Parameter("some:key", "value");
            parameterMap = new HashMap();
            parameterMap.put(param.getKey(), param.getValue());

            Message.createMessage(new ParameterList(parameterMap));

            fail("A MessageException should be thrown " +
                    "if the key/values contain invalid characters");
        } catch (MessageException expected) {
            assertTrue(true);
        }
        try
        {
            // newline in key
            param = new Parameter("some\nkey\n", "value");
            parameterMap = new HashMap();
            parameterMap.put(param.getKey(), param.getValue());

            Message.createMessage(new ParameterList(parameterMap));

            fail("A MessageException should be thrown " +
                    "if the key/values contain invalid characters");
        } catch (MessageException expected) {
            assertTrue(true);
        }
        try
        {
            // newline in value
            param = new Parameter("key", "val\nue");
            parameterMap = new HashMap();
            parameterMap.put(param.getKey(), param.getValue());

            Message.createMessage(new ParameterList(parameterMap));

            fail("A MessageException should be thrown " +
                    "if the key/values contain invalid characters");
        } catch (MessageException expected) {
            assertTrue(true);
        }
        try
        {
            // all of the above
            param = new Parameter("some:\nkey", "value\n");
            parameterMap = new HashMap();
            parameterMap.put(param.getKey(), param.getValue());

            Message.createMessage(new ParameterList(parameterMap));

            fail("A MessageException should be thrown " +
                    "if the key/values contain invalid characters");
        } catch (MessageException expected) {
            assertTrue(true);
        }
    }

}
