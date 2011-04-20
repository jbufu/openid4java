/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.util.List;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class ParameterListTest extends TestCase
{
    private ParameterList _parameterList;

    public ParameterListTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        _parameterList = new ParameterList();

        _parameterList.set(new Parameter("key1", "value1"));
        _parameterList.set(new Parameter("key1", "value2"));
        _parameterList.set(new Parameter("key2", "value1"));
    }

    public void tearDown() throws Exception
    {
        _parameterList = null;
    }

    public void testEquals() throws Exception
    {
        ParameterList parameterList2 = new ParameterList();

        parameterList2.set(new Parameter("key1", "value1"));
        parameterList2.set(new Parameter("key1", "value2"));
        parameterList2.set(new Parameter("key2", "value1"));

        assertEquals(_parameterList, parameterList2);
        assertNotSame(_parameterList, parameterList2);

        parameterList2 = new ParameterList();

        parameterList2.set(new Parameter("key2", "value1"));
        parameterList2.set(new Parameter("key1", "value1"));
        parameterList2.set(new Parameter("key1", "value2"));
        parameterList2.set(new Parameter("key3", "value1"));
        parameterList2.set(new Parameter("key3", "value2"));
        parameterList2.set(new Parameter("key3", "value1"));
        parameterList2.removeParameters("key3");

        assertEquals(_parameterList, parameterList2);
        assertNotSame(_parameterList, parameterList2);

        parameterList2 = new ParameterList();

        // null not supported in compareTo()
        //parameterList2.set(new Parameter(null, null));
        //parameterList2.set(new Parameter(null, ""));
        //parameterList2.set(new Parameter("", null));
        parameterList2.set(new Parameter("", ""));
    }

    public void testHashCode() throws Exception
    {
        ParameterList parameterList2 = new ParameterList();

        parameterList2.set(new Parameter("key1", "value1"));
        parameterList2.set(new Parameter("key1", "value2"));
        parameterList2.set(new Parameter("key2", "value1"));

        assertEquals(_parameterList.hashCode(), parameterList2.hashCode());
        assertNotSame(_parameterList, parameterList2);

        parameterList2 = new ParameterList();

        parameterList2.set(new Parameter("key2", "value1"));
        parameterList2.set(new Parameter("key1", "value1"));
        parameterList2.set(new Parameter("key1", "value2"));
        parameterList2.set(new Parameter("key3", "value1"));
        parameterList2.set(new Parameter("key3", "value2"));
        parameterList2.set(new Parameter("key3", "value1"));
        parameterList2.removeParameters("key3");

        assertEquals(_parameterList.hashCode(), parameterList2.hashCode());
        assertNotSame(_parameterList, parameterList2);
    }

    public void testCopyConstructor()
    {
        ParameterList parameterList2 = new ParameterList(_parameterList);

        assertEquals(2, _parameterList.getParameters().size());
        assertEquals(2, parameterList2.getParameters().size());

        _parameterList.removeParameters("key1");

        assertEquals(1, _parameterList.getParameters().size());
        assertEquals(2, parameterList2.getParameters().size());
    }

    public void testAdd() throws Exception
    {
        assertEquals(2, _parameterList.getParameters().size());

        _parameterList.set(new Parameter("key3", "value1"));

        assertEquals(3, _parameterList.getParameters().size());
    }

    public void testGetParameter() throws Exception
    {
        Parameter parameter = _parameterList.getParameter("key2");

        assertNotNull(parameter);
        assertEquals("value1", parameter.getValue());
    }

    public void testGetParameterNull() throws Exception
    {
        Parameter parameter = _parameterList.getParameter("key3");

        assertNull(parameter);
    }

    public void testGetParameterValue() throws Exception
    {
        String value = _parameterList.getParameterValue("key2");

        assertNotNull(value);
        assertEquals("value1", value);
    }

    public void testGetParameters() throws Exception
    {
        List parameters = _parameterList.getParameters();

        assertEquals(2, parameters.size());
    }


    public void testGetParameters1Null() throws Exception
    {
        assertNull(_parameterList.getParameterValue("key3"));
    }

    public void testRemoveParameters() throws Exception
    {
        _parameterList.removeParameters("key1");

        assertEquals(1, _parameterList.getParameters().size());

        _parameterList.removeParameters("key2");

        assertEquals(0, _parameterList.getParameters().size());
    }

    public void testReplaceParameters() throws Exception
    {
        _parameterList.set(new Parameter("key2", "value3"));

        assertEquals("value3", _parameterList.getParameter("key2").getValue());
    }

    public void testHasParameter() throws Exception
    {
        assertTrue(_parameterList.hasParameter("key1"));
        assertTrue(_parameterList.hasParameter("key2"));

        assertFalse(_parameterList.hasParameter("key3"));
    }

    public void testCreateFromQueryString() throws Exception
    {
        ParameterList createdParameterList = ParameterList.createFromQueryString("key1=value%31&key1=value2&key2=value1");

        assertEquals(_parameterList, createdParameterList);

        createdParameterList = ParameterList.createFromQueryString("key1=value%31&key1=&key2=value1");

        assertEquals("", ((Parameter) createdParameterList.getParameters()
                .get(0)).getValue() );

        createdParameterList = ParameterList.createFromQueryString("key1=value%31&key1=&key2=");

        assertEquals("", createdParameterList.getParameterValue("key2"));
    }

    public void testCreateFromKeyValueForm() throws Exception
    {
        ParameterList createdParameterList = ParameterList.createFromKeyValueForm("key1:value1\nkey1:value2\nkey2:value1");

        assertEquals(_parameterList, createdParameterList);

        createdParameterList = ParameterList.createFromKeyValueForm("key1:value1\nkey1:\nkey2:value1");

        assertEquals("", ((Parameter) createdParameterList.getParameters().get(0)).getValue() );

        createdParameterList = ParameterList.createFromKeyValueForm("key1:value1\nkey1:\nkey2:");

        assertEquals("", createdParameterList.getParameterValue("key2"));

        createdParameterList = ParameterList.createFromKeyValueForm("key1:value1\nkey2:value:2");

        assertEquals("value:2", createdParameterList.getParameterValue("key2"));

        createdParameterList = ParameterList.createFromKeyValueForm("key1:value1\nkey2:value2\n");

        assertEquals("value2", createdParameterList.getParameterValue("key2"));


    }

    public static Test suite()
    {
        return new TestSuite(ParameterListTest.class);
    }
}
