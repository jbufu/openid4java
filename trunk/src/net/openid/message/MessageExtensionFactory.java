/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.message;

/**
 * Keeps track of the OpenID extension for which there is an implementation
 * available.
 *
 * @see MessageExtension
 * @see Message
 * @author Marius Scurtescu, Johnny Bufu
 */
public interface MessageExtensionFactory
{
    public MessageExtension create(String alias, ParameterList parameterList) throws MessageException;
}
