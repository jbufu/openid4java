/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.message;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class ParameterTest extends TestCase
{
    public ParameterTest(String name)
    {
        super(name);
    }

    public void testEquals() throws Exception
    {
        Parameter parameter1 = new Parameter("key", "value");
        Parameter parameter2 = new Parameter("key", "value");

        assertEquals(parameter1, parameter2);
        assertNotSame(parameter1, parameter2);

        parameter1 = new Parameter("", "value");
        parameter2 = new Parameter("", "value");

        assertEquals(parameter1, parameter2);
        assertNotSame(parameter1, parameter2);

        parameter1 = new Parameter("", "");
        parameter2 = new Parameter("", "");

        assertEquals(parameter1, parameter2);
        assertNotSame(parameter1, parameter2);

        parameter1 = new Parameter(null, "");
        parameter2 = new Parameter(null, "");

        assertEquals(parameter1, parameter2);
        assertNotSame(parameter1, parameter2);

        parameter1 = new Parameter(null, null);
        parameter2 = new Parameter(null, null);

        assertEquals(parameter1, parameter2);
        assertNotSame(parameter1, parameter2);

        parameter1 = new Parameter("key", "value1");
        parameter2 = new Parameter("key", "value2");

        assertFalse(parameter1.equals(parameter2));
        assertNotSame(parameter1, parameter2);
    }

    public void testHashCode() throws Exception
    {
        Parameter parameter1 = new Parameter("key", "value");
        Parameter parameter2 = new Parameter("key", "value");

        assertEquals(parameter1.hashCode(), parameter2.hashCode());
        assertNotSame(parameter1, parameter2);

        parameter1 = new Parameter("", "value");
        parameter2 = new Parameter("", "value");

        assertEquals(parameter1.hashCode(), parameter2.hashCode());
        assertNotSame(parameter1, parameter2);

        parameter1 = new Parameter("", "");
        parameter2 = new Parameter("", "");

        assertEquals(parameter1.hashCode(), parameter2.hashCode());
        assertNotSame(parameter1, parameter2);

        parameter1 = new Parameter(null, "");
        parameter2 = new Parameter(null, "");

        assertEquals(parameter1.hashCode(), parameter2.hashCode());
        assertNotSame(parameter1, parameter2);

        parameter1 = new Parameter(null, null);
        parameter2 = new Parameter(null, null);

        assertEquals(parameter1.hashCode(), parameter2.hashCode());
        assertNotSame(parameter1, parameter2);
    }

    public void testGetName() throws Exception
    {
        Parameter parameter = new Parameter(null, "value");

        assertNull(parameter.getKey());

        parameter = new Parameter("", "value");

        assertEquals("", parameter.getKey());

        parameter = new Parameter("key", "value");

        assertEquals("key", parameter.getKey());
    }

    public void testGetValue() throws Exception
    {
        Parameter parameter = new Parameter("key", null);

        assertNull(parameter.getValue());

        parameter = new Parameter("key", "");

        assertEquals("", parameter.getValue());

        parameter = new Parameter("key", "value");

        assertEquals("value", parameter.getValue());
    }

    public void testNotAllowedChars()
    {
        try
        {
            // semicolon in key
            new Parameter("some:key", "value");
            fail("A MessageException should be thrown " +
                    "if the key/values contain invalid characters");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        try
        {
            // newline in key
            new Parameter("some\nkey\n", "value");
            fail("A MessageException should be thrown " +
                    "if the key/values contain invalid characters");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        try
        {
            // newline in value
            new Parameter("key", "val\nue");
            fail("A MessageException should be thrown " +
                    "if the key/values contain invalid characters");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        try
        {
            // all of the above
            new Parameter("some:\nkey", "value\n");
            fail("A MessageException should be thrown " +
                    "if the key/values contain invalid characters");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }


    public static Test suite()
    {
        return new TestSuite(ParameterTest.class);
    }
}
