/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message;

/**
 * Factory interface for creating message extension objects for a specific
 * message extension type URI.
 *
 * @see MessageExtension
 * @see Message
 * @author Marius Scurtescu, Johnny Bufu
 */
public interface MessageExtensionFactory
{

    /**
     * Gets the extension type URI of the extension factory.
     */
    public String getTypeUri();

    /**
     * Builds a MessageExtension from a parameter list containing the
     * extension-specific parameters.
     * <p>
     * The parameters MUST NOT contain the openid.<extension_alias> prefix.
     *
     * @param parameterList     The extension parameters with the
     *                          openid.<extension_alias> prefix removed.
     * @param isRequest         Indicates whether the parameters were extracted
     *                          from an openid request (true), or from an openid
     *                          response (false). This may assist the factory
     *                          implementation in determining what object type
     *                          to instantiate.
     * @return                  MessageExtension implementation for the supplied
     *                          extension parameters.
     */
    public MessageExtension getExtension(
            ParameterList parameterList, boolean isRequest)
            throws MessageException;
}
