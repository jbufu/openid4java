/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.infocard;

/**
 * Enumeration class with the defined OpenID token types.
 */
public class OpenIDTokenType
{
    /**
     * OpenID 1.1 token type.
     */
    public static final OpenIDTokenType OPENID11_TOKEN =
        new OpenIDTokenType("http://specs.openid.net/auth/1.1");

    /**
     * OpenID 2.0 token type.
     */
    public static final OpenIDTokenType OPENID20_TOKEN =
        new OpenIDTokenType("http://specs.openid.net/auth/2.0");

    /**
     * The OpenID Identifier claim type.
     */
    public static final String OPENID_CLAIM =
        "http://schema.openid.net/2007/05/claims/identifier";

    /**
     * Token URI value.
     */
    private final String _tokenTypeUri;

    /**
     * Constructs a token type for the given URI value.
     */
    private OpenIDTokenType(String uriValue)
    {
        _tokenTypeUri = uriValue;
    }

    /**
     * Gets the URI string value for the token type.
     * @return  String representation of the token URI type.
     */
    public String toString()
    {
        return _tokenTypeUri;
    }
}
