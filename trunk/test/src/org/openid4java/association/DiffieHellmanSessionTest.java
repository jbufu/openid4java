/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.association;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import javax.crypto.spec.DHParameterSpec;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.interfaces.DHPrivateKey;
import java.security.KeyPair;
import java.security.GeneralSecurityException;
import java.math.BigInteger;

import org.apache.commons.codec.binary.Base64;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class DiffieHellmanSessionTest extends TestCase
{
    public DiffieHellmanSessionTest(String name)
    {
        super(name);
    }

    public void testGetDefaultParameterSha1() throws Exception
    {
        DHParameterSpec parameterSpec = DiffieHellmanSession.getDefaultParameter();

        assertNotNull(parameterSpec);

        assertEquals(2, parameterSpec.getG().intValue());
        assertEquals(DiffieHellmanSession.DEFAULT_MODULUS_HEX.length() * 4, parameterSpec.getP().bitLength());
    }

    public void testGetDefaultParameterSha256() throws Exception
    {
        DHParameterSpec parameterSpec = DiffieHellmanSession.getDefaultParameter();

        assertNotNull(parameterSpec);

        assertEquals(2, parameterSpec.getG().intValue());
        assertEquals(DiffieHellmanSession.DEFAULT_MODULUS_HEX.length() * 4, parameterSpec.getP().bitLength());
    }

    public void testGenerateRandomParameterSha1() throws Exception
    {
        DHParameterSpec parameterSpec = DiffieHellmanSession.generateRandomParameter(512, 256);

        assertNotNull(parameterSpec);

        assertEquals(512, parameterSpec.getP().bitLength());
    }

    public void testGenerateRandomParameterSha256() throws Exception
    {
        DHParameterSpec parameterSpec = DiffieHellmanSession.generateRandomParameter(512, 256);

        assertNotNull(parameterSpec);

        assertEquals(512, parameterSpec.getP().bitLength());
    }

    public void testGenerateKeyPairSha1Default()
    {
        DHParameterSpec parameterSpec = DiffieHellmanSession.getDefaultParameter();

        KeyPair keyPair = DiffieHellmanSession.generateKeyPair(parameterSpec);

        assertNotNull(keyPair);
    }

    public void testGenerateKeyPairSha256Default()
    {
        DHParameterSpec parameterSpec = DiffieHellmanSession.getDefaultParameter();

        KeyPair keyPair = DiffieHellmanSession.generateKeyPair(parameterSpec);

        assertNotNull(keyPair);
    }

    public void testGenerateKeyPairSha1Random()
    {
        DHParameterSpec parameterSpec = DiffieHellmanSession.generateRandomParameter(512, 256);

        KeyPair keyPair = DiffieHellmanSession.generateKeyPair(parameterSpec);

        assertNotNull(keyPair);
    }

    public void testGenerateKeyPairSha256Random()
    {
        DHParameterSpec parameterSpec = DiffieHellmanSession.generateRandomParameter(512, 256);

        KeyPair keyPair = DiffieHellmanSession.generateKeyPair(parameterSpec);

        assertNotNull(keyPair);
    }

    public void testPublicKeyConversion() throws AssociationException
    {
        DHParameterSpec dhParameterSpec = DiffieHellmanSession.getDefaultParameter();

        DiffieHellmanSession diffieHellmanSession = DiffieHellmanSession.create(AssociationSessionType.DH_SHA1, dhParameterSpec);

        String publicKeyBase64 = diffieHellmanSession.getPublicKey();

        assertNotNull(publicKeyBase64);

        DHPublicKey publicKey = diffieHellmanSession.stringToPublicKey(publicKeyBase64);

        assertNotNull(publicKey);
        assertEquals(publicKeyBase64, DiffieHellmanSession.publicKeyToString(publicKey));
    }

    public void testEncryptDecryptMacKeySha1() throws GeneralSecurityException, AssociationException
    {
        DHParameterSpec dhParameterSpec = DiffieHellmanSession.getDefaultParameter();

        assertNotNull(dhParameterSpec);

        DiffieHellmanSession consumerDiffieHellmanSession = DiffieHellmanSession.create(AssociationSessionType.DH_SHA1, dhParameterSpec);
        byte[] macKey = Association.generateMacKey(Association.HMAC_SHA1_ALGORITHM, Association.HMAC_SHA1_KEYSIZE).getEncoded();

        testEncryptDecryptMacKey(consumerDiffieHellmanSession, macKey);
    }

    public void testEncryptDecryptMacKeySha1Random() throws GeneralSecurityException, AssociationException
    {
        DHParameterSpec dhParameterSpec = DiffieHellmanSession.generateRandomParameter(512, 256);

        assertNotNull(dhParameterSpec);

        DiffieHellmanSession consumerDiffieHellmanSession = DiffieHellmanSession.create(AssociationSessionType.DH_SHA1, dhParameterSpec);
        byte[] macKey = Association.generateMacKey(Association.HMAC_SHA1_ALGORITHM, Association.HMAC_SHA1_KEYSIZE).getEncoded();

        testEncryptDecryptMacKey(consumerDiffieHellmanSession, macKey);
    }

    public void testEncryptDecryptMacKeySha256() throws GeneralSecurityException, AssociationException
    {
        DHParameterSpec dhParameterSpec = DiffieHellmanSession.getDefaultParameter();

        assertNotNull(dhParameterSpec);

        DiffieHellmanSession consumerDiffieHellmanSession = DiffieHellmanSession.create(AssociationSessionType.DH_SHA256, dhParameterSpec);
        byte[] macKey = Association.generateMacKey(Association.HMAC_SHA256_ALGORITHM, Association.HMAC_SHA256_KEYSIZE).getEncoded();

        testEncryptDecryptMacKey(consumerDiffieHellmanSession, macKey);
    }

    public void testEncryptDecryptMacKeySha256Random() throws GeneralSecurityException, AssociationException
    {
        DHParameterSpec dhParameterSpec = DiffieHellmanSession.generateRandomParameter(512, 256);

        assertNotNull(dhParameterSpec);

        DiffieHellmanSession consumerDiffieHellmanSession = DiffieHellmanSession.create(AssociationSessionType.DH_SHA256, dhParameterSpec);
        byte[] macKey = Association.generateMacKey(Association.HMAC_SHA256_ALGORITHM, Association.HMAC_SHA256_KEYSIZE).getEncoded();

        testEncryptDecryptMacKey(consumerDiffieHellmanSession, macKey);
    }

    private void testEncryptDecryptMacKey(DiffieHellmanSession consumerDiffieHellmanSession, byte[] macKey)
            throws AssociationException
    {
        AssociationSessionType type = consumerDiffieHellmanSession.getType();
        String modulusBase64   = consumerDiffieHellmanSession.getModulus();
        String generatorBase64 = consumerDiffieHellmanSession.getGenerator();
        String consumerPublicKeyBase64 = consumerDiffieHellmanSession.getPublicKey();
        String consumerPrivateKeyBase64 = privateKeyToString(consumerDiffieHellmanSession.getPrivateKey());

        DiffieHellmanSession serverDiffieHellmanSession = DiffieHellmanSession.create(type, modulusBase64, generatorBase64);

        assertEquals(type, serverDiffieHellmanSession.getType());
        assertEquals(modulusBase64, serverDiffieHellmanSession.getModulus());
        assertEquals(generatorBase64, serverDiffieHellmanSession.getGenerator());

        String serverPublicKeyBase64 = serverDiffieHellmanSession.getPublicKey();
        String serverPrivateKeyBase64 = privateKeyToString(serverDiffieHellmanSession.getPrivateKey());
        String macKeyBase64 = new String(Base64.encodeBase64(macKey));

        String encMacBase64 = serverDiffieHellmanSession.encryptMacKey(macKey, consumerPublicKeyBase64);

        byte[] macKey2 = consumerDiffieHellmanSession.decryptMacKey(encMacBase64, serverPublicKeyBase64);

        assertEquals(macKey.length, macKey2.length);

        for (int i = 0; i < macKey.length; i++)
        {
            assertEquals(macKey[i], macKey2[i]);
        }
    }

    public void testPublicKey() throws AssociationException
    {
        DHParameterSpec dhParameterSpec = DiffieHellmanSession.getDefaultParameter();

        DiffieHellmanSession diffieHellmanSession = DiffieHellmanSession.create(AssociationSessionType.DH_SHA1, dhParameterSpec);

        String dhPublicKeyBase64 = diffieHellmanSession.getPublicKey();

        DHPublicKey dhPublicKey = diffieHellmanSession.stringToPublicKey(dhPublicKeyBase64);

        BigInteger two = new BigInteger("2");
        BigInteger y = dhPublicKey.getY();
        BigInteger p = dhParameterSpec.getP();

        assertTrue(y.compareTo(two) != -1);
        assertTrue(y.compareTo(p) == -1);
    }

    private static String privateKeyToString(DHPrivateKey dhPrivateKey)
    {
        return new String(Base64.encodeBase64(dhPrivateKey.getX().toByteArray()));
    }

    public static Test suite()
    {
        return new TestSuite(DiffieHellmanSessionTest.class);
    }
}
