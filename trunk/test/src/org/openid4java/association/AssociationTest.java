/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.association;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class AssociationTest extends TestCase
{
    public AssociationTest(String name)
    {
        super(name);
    }

    public void testGenerateSha1()
    {
        SecretKey secretKey = Association.generateMacSha1Key();

        assertNotNull(secretKey);
        assertTrue(secretKey instanceof SecretKeySpec);

        SecretKeySpec secretKeySpec = (SecretKeySpec) secretKey;

        assertEquals(Association.HMAC_SHA1_ALGORITHM.toUpperCase(), secretKeySpec.getAlgorithm().toUpperCase());
        assertEquals(20, secretKeySpec.getEncoded().length);
    }

    public void testGenerateSha256()
    {
        if (Association.isHmacSha256Supported())
        {
            SecretKey secretKey = Association.generateMacSha256Key();

            assertNotNull(secretKey);
            assertTrue(secretKey instanceof SecretKeySpec);

            SecretKeySpec secretKeySpec = (SecretKeySpec) secretKey;

            assertEquals(Association.HMAC_SHA256_ALGORITHM.toUpperCase(), secretKeySpec.getAlgorithm().toUpperCase());
            assertEquals(32, secretKeySpec.getEncoded().length);
        }
    }

    public void testSignSha1() throws AssociationException
    {
        Association association = Association.generate(Association.TYPE_HMAC_SHA1, "test", 100);

        String macKeyBase64 = new String(Base64.encodeBase64(association.getMacKey().getEncoded()));
        String text = "key1:value1\nkey2:value2\n";

        String signature = association.sign(text);

        assertTrue(association.verifySignature(text, signature));
    }

    public void testSignSha256() throws AssociationException
    {
        Association association = Association.generate(Association.TYPE_HMAC_SHA256, "test", 100);

        String macKeyBase64 = new String(Base64.encodeBase64(association.getMacKey().getEncoded()));
        String text = "key1:value1\nkey2:value2\n";

        String signature = association.sign(text);

        assertTrue(association.verifySignature(text, signature));
    }

    public static Test suite()
    {
        return new TestSuite(AssociationTest.class);
    }
}
