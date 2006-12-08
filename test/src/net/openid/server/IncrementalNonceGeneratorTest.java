/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.server;

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
