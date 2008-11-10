/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.util;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.util.Date;
import java.text.ParseException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class InternetDateFormatTest extends TestCase
{
    InternetDateFormat _dateFormat;

    public InternetDateFormatTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        _dateFormat = new InternetDateFormat();
    }

    public void testFormat()
    {
        Date date0 = new Date(0);

        assertEquals("1970-01-01T00:00:00Z", _dateFormat.format(date0));
    }

    public void testParse() throws ParseException
    {
        Date date0 = new Date(0);

        assertEquals(date0, _dateFormat.parse("1970-01-01T00:00:00Z"));
        assertEquals(date0, _dateFormat.parse("1970-01-01t00:00:00z"));
    }

    public static Test suite()
    {
        return new TestSuite(InternetDateFormatTest.class);
    }
}
