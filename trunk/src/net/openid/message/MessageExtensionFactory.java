/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.message;

/**
 * Factory that creates message extension objects for a specific URI.
 *
 * @see MessageExtension
 * @see Message
 * @author Marius Scurtescu, Johnny Bufu
 */
public interface MessageExtensionFactory
{
    public String getTypeUri();

    public MessageExtension create(String alias, ParameterList parameterList) throws MessageException;
}
