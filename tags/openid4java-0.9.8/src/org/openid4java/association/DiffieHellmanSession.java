/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.association;

import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHGenParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.interfaces.DHPrivateKey;
import java.math.BigInteger;
import java.security.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class DiffieHellmanSession
{
    private static Log _log = LogFactory.getLog(DiffieHellmanSession.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    public static final String DEFAULT_MODULUS_HEX =
        "DCF93A0B883972EC0E19989AC5A2CE310E1D37717E8D9571BB7623731866E61E" +
        "F75A2E27898B057F9891C2E27A639C3F29B60814581CD3B2CA3986D268370557" +
        "7D45C2E7E52DC81C7A171876E5CEA74B1448BFDFAF18828EFD2519F14E45E382" +
        "6634AF1949E5B535CC829A483B8A76223E5D490A257F05BDFF16F2FB22C583AB";
    public static final String DEFAULT_MODULUS_BASE64 =
        "ANz5OguIOXLsDhmYmsWizjEOHTdxfo2Vcbt2I3MYZuYe91ouJ4mLBX+YkcLiemOc" +
        "Pym2CBRYHNOyyjmG0mg3BVd9RcLn5S3IHHoXGHblzqdLFEi/368Ygo79JRnxTkXj" +
        "gmY0rxlJ5bU1zIKaSDuKdiI+XUkKJX8Fvf8W8vsixYOr";

    public static final long   DEFAULT_GENERATOR = 2;
    public static final String DEFAULT_GENERATOR_BASE64 = "Ag==";

    public static final String ALGORITHM = "DH";
    public static final String H_ALGORITHM_SHA1 = "SHA-1";
    public static final String H_ALGORITHM_SHA256 = "SHA-256";

    private AssociationSessionType _type;
    private DHParameterSpec _dhParameterSpec;
    private KeyPair _keyPair;
    private MessageDigest _hDigest;

    private DiffieHellmanSession(AssociationSessionType type,
                                 DHParameterSpec dhParameterSpec)
            throws AssociationException
    {
        _type            = type;
        _dhParameterSpec = dhParameterSpec;
        _keyPair         = generateKeyPair(dhParameterSpec);

        try
        {
            _hDigest = MessageDigest.getInstance(_type.getHAlgorithm());
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new AssociationException("Unsupported H algorithm: " +
                    _type.getHAlgorithm(), e);
        }
    }

    public String toString()
    {
        return _type + " base: " + _dhParameterSpec.getG()
                + " modulus: " + _dhParameterSpec.getP();
    }

    public static DiffieHellmanSession create(AssociationSessionType type,
                                              String modulusBase64,
                                              String generatorBase64)
            throws AssociationException
    {
        byte[] modulus   = Base64.decodeBase64(modulusBase64.getBytes());
        byte[] generator = Base64.decodeBase64(generatorBase64.getBytes());

        BigInteger p = new BigInteger(modulus);
        BigInteger g = new BigInteger(generator);

        DHParameterSpec dhParameterSpec = new DHParameterSpec(p, g);

        return create(type, dhParameterSpec);
    }

    public static DiffieHellmanSession create(AssociationSessionType type,
                                              DHParameterSpec dhParameterSpec)
            throws AssociationException
    {

        DiffieHellmanSession dh = new DiffieHellmanSession(type, dhParameterSpec);

        if (DEBUG) _log.debug("Created DH session: " + dh);

        return dh;
    }

    public static DHParameterSpec getDefaultParameter()
    {
        BigInteger p = new BigInteger(DEFAULT_MODULUS_HEX, 16);
        BigInteger g = BigInteger.valueOf(DEFAULT_GENERATOR);

        return new DHParameterSpec(p, g);
    }

    public static DHParameterSpec generateRandomParameter(int primeSize, int keySize)
    {
        try
        {
            AlgorithmParameterGenerator paramGen =
                    AlgorithmParameterGenerator.getInstance(ALGORITHM);

            DHGenParameterSpec genParameterSpec =
                    new DHGenParameterSpec(primeSize, keySize);

            paramGen.init(genParameterSpec);

            AlgorithmParameters params = paramGen.generateParameters();

            DHParameterSpec result = (DHParameterSpec)
                    params.getParameterSpec(DHParameterSpec.class);

            if (DEBUG) _log.debug("Generated random DHParameterSpec, base: "
                    + result.getG() + ", modulus: " + result.getP());

            return result;
        }
        catch (GeneralSecurityException e)
        {
            _log.error("Cannot generate DH params for primeSize: "
                    + primeSize + " keySize: " + keySize, e);
            return null;
        }
    }

    protected static KeyPair generateKeyPair(DHParameterSpec dhSpec)
    {
        try
        {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);

            keyGen.initialize(dhSpec);

            return keyGen.generateKeyPair();
        }
        catch (GeneralSecurityException e)
        {
            _log.error("Cannot generate key pair for DHParameterSpec, base: "
                    + dhSpec.getG() + ", modulus: "  + dhSpec.getP() );

            return null;
        }
    }

    public AssociationSessionType getType()
    {
        return _type;
    }

    /**
     * Gets the modulus for the Diffie-Hellman key echange.
     * This is the value passed in the <b>openid.dh_modulus</b> association
     * request parameter.
     *
     * @return  The base 64 encoded two's-complement representation of the
     *          modulus: <code>base64(btwoc(p))</code>
     */
    public String getModulus()
    {
        BigInteger p  = _dhParameterSpec.getP();

        return new String(Base64.encodeBase64(p.toByteArray()));
    }

    /**
     * Gets the generator for the Diffie-Hellman key echange.
     * This is the value passed in the <b>openid.dh_gen</b> association
     * request parameter.
     *
     * @return  The base 64 encoded two's-complement representation of the
     *          generator: <code>base64(btwoc(g))</code>
     */
    public String getGenerator()
    {
        BigInteger g  = _dhParameterSpec.getG();

        return new String(Base64.encodeBase64(g.toByteArray()));
    }

    /**
     * Get the Diffie-Hellman public key.
     * This is the value passed in the <b>openid.dh_consumer_public</b>
     * association request parameter and the value passed in the
     * <b>openid.dh_server_public</b> association response parameter.
     *
     * @return  The base 64 encoded two's-complement representation of the
     *          public key: <code>base64(btwoc(g ^ x mod p))</code>
     */
    public String getPublicKey()
    {
        DHPublicKey publicKey = (DHPublicKey) _keyPair.getPublic();

        return publicKeyToString(publicKey);
    }

    protected DHPrivateKey getPrivateKey()
    {
        return (DHPrivateKey) _keyPair.getPrivate();
    }

    /**
     * Encrypts the association MAC key. The encryption takes palce on the
     * server side (aka OP). This is the value passed in the
     * <b>openid.enc_mac_key</b> association response parameter.
     *
     * @param macKey                    The MAC key in binary format.
     * @param consumerPublicKeyBase64   The base 64 encoding of the consumer
     *                                  Diffie-Hellman public key. This is the
     *                                  value passed in the
     *                                  <b>openid.dh_consumer_public</b>
     *                                  association request parameter.
     * @return                          The base 64 encoded two's-complement
     *                                  representation of the encrypted mac key:
     *                <code>base64(H(btwoc(g ^ (xa * xb) mod p)) XOR MAC)</code>
     * @throws AssociationException     if the lengths of the mac key and digest
     *                                  of Diffie-Hellman shared secred do not
     *                                  match.
     */
    public String encryptMacKey(byte[] macKey, String consumerPublicKeyBase64)
            throws AssociationException
    {
        byte[] hzz = getDigestedZZ(consumerPublicKeyBase64);

        if (hzz.length != macKey.length)
            throw new AssociationException(
                    "MAC key legth different from shared secret digest length!");

        byte[] encMacKey = new byte[hzz.length];

        for (int i = 0; i < hzz.length; i++)
        {
            byte b1 = hzz[i];
            byte b2 = macKey[i];

            encMacKey[i] = (byte) (b1 ^ b2);
        }

        String encMacKeyBase64 = new String(Base64.encodeBase64(encMacKey));

        if (DEBUG) _log.debug("Encrypted MAC key Base64: " + encMacKeyBase64);

        return encMacKeyBase64;
    }

    /**
     * Decrypts the association AMC key. The decryption takes palce on the
     * consumer side (aka RP).
     *
     * @param encMacKeyBase64           The base 64 encoded two's-complement
     *                                  representation of the encrypted mac key:
     *               <code>base64(H(btwoc(g ^ (xa * xb) mod p)) XOR MAC)</code>.
     *                                  This is the value passed in the
     *                                  <b>openid.enc_mac_key</b> association
     *                                  response parameter.
     * @param serverPublicKeyBase64     The base 64 encoding of the server
     *                                  Diffie-Hellman public key. This is the
     *                                  value passed in the
     *                                  <b>openid.dh_server_public</b>
     *                                  association response parameter.
     * @return                          The MAC key in binary format.
     * @throws AssociationException     if the lengths of the encrypted mac key
     *                                  and digest of Diffie-Hellman shared
     *                                  secret do not match.
     */
    public byte[] decryptMacKey(String encMacKeyBase64, String serverPublicKeyBase64)
            throws AssociationException
    {
        byte[] hzz = getDigestedZZ(serverPublicKeyBase64);
        byte[] encMacKey = Base64.decodeBase64(encMacKeyBase64.getBytes());

        if (hzz.length != encMacKey.length)
            throw new AssociationException(
                    "Encrypted MAC key legth different from shared secret digest length!");

        byte[] macKey = new byte[hzz.length];

        for (int i = 0; i < hzz.length; i++)
        {
            byte b1 = hzz[i];
            byte b2 = encMacKey[i];

            macKey[i] = (byte) (b1 ^ b2);
        }

        if (DEBUG) _log.debug("Decrypted MAC key Base64: "
                + new String(Base64.encodeBase64(macKey)));

        return macKey;
    }

    protected static String publicKeyToString(DHPublicKey publicKey)
    {
        return new String(Base64.encodeBase64(publicKey.getY().toByteArray()));
    }

    protected DHPublicKey stringToPublicKey(String publicKeyBase64)
    {
        try
        {
            byte[] yBinary = Base64.decodeBase64(publicKeyBase64.getBytes());
            BigInteger y = new BigInteger(yBinary);

            DHPublicKeySpec dhPublicKeySpec = new DHPublicKeySpec(
                    y, _dhParameterSpec.getP(), _dhParameterSpec.getG() );

            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);

            return (DHPublicKey) keyFactory.generatePublic(dhPublicKeySpec);
        }
        catch (GeneralSecurityException e)
        {
            _log.error("Cannot create PublicKey object from: " + publicKeyBase64, e);

            return null;
        }
    }

    protected byte[] getDigestedZZ(String otherPublicKeyBase64)
    {
        DHPublicKey  dhPublicKey  = stringToPublicKey(otherPublicKeyBase64);
        DHPrivateKey dhPrivateKey = getPrivateKey();
        BigInteger xa = dhPrivateKey.getX();
        BigInteger yb = dhPublicKey.getY();
        BigInteger p  = _dhParameterSpec.getP();

        BigInteger zz = yb.modPow(xa, p);

        return _hDigest.digest(zz.toByteArray());
    }

    private static boolean isDhSupported()
    {
        try
        {
            AlgorithmParameterGenerator.getInstance(ALGORITHM);
            KeyPairGenerator.getInstance(ALGORITHM);
            KeyFactory.getInstance(ALGORITHM);

            return true;
        }
        catch (NoSuchAlgorithmException e)
        {
            return false;
        }
    }

    public static boolean isDhSupported(AssociationSessionType type)
    {
        String hAlg = type.getHAlgorithm();

        if (hAlg == null) // no encryption sessions
            return true;
        else
            return isDhShaSupported(hAlg);

    }

    public static boolean isDhShaSupported(String shaAlgorithm)
    {
        if (!isDhSupported())
            return false;

        try
        {
            MessageDigest.getInstance(shaAlgorithm);

            return true;
        }
        catch (NoSuchAlgorithmException e)
        {
            return false;
        }
    }

    public static boolean isDhSha1Supported()
    {
        return isDhShaSupported(H_ALGORITHM_SHA1);
    }

    public static boolean isDhSha256Supported()
    {
        return isDhShaSupported(H_ALGORITHM_SHA256);
    }
}
