/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.server;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class IncrementalNonceGeneratorTest extends AbstractNonceGeneratorTest
{
    public IncrementalNonceGeneratorTest(String name)
    {
        super(name);
    }

    public NonceGenerator createGenerator()
    {
        return new IncrementalNonceGenerator();
    }

    public static Test suite()
    {
        return new TestSuite(IncrementalNonceGeneratorTest.class);
    }
}
