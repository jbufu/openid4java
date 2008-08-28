/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message;

/**
 * Interface for building OpenID extensions.
 * <p>
 * Classes that implement this interface should provide a default constructor
 * and register their Type URIs with the MessageExtensionFactory.
 *
 * @see MessageExtensionFactory Message
 * @author Marius Scurtescu, Johnny Bufu
 */
public interface MessageExtension
{
    /**
     * Gets the TypeURI that identifies a extension to the OpenID protocol.
     */
    public String getTypeUri();

    /**
     * Gets the extension-specific parameters.
     * <p>
     * Implementations MUST NOT prefix the parameter names with
     * "openid.<extension_alias>". The alias is managed internally by the Message class,
     * when a extension is attached to an OpenID messaage.
     *
     * @see Message
     */
    public ParameterList getParameters();

    /**
     * Sets the extension-specific parameters.
     * <p>
     * Implementations MUST NOT prefix the parameter names with
     * "openid.<extension_alias>". The alias is managed internally by the Message class,
     * when a extension is attached to an OpenID messaage.

     * @param params
     * @see Message
     */
    public void  setParameters(ParameterList params);

    /**
     * Used by the core OpenID authentication implementation to learn whether
     * an extension provies authentication services.
     * <p>
     * If the extension provides authentication services,
     * the 'openid.identity' and 'openid.signed' parameters are optional.
     *
     * @return  True if the extension provides authentication services,
     *          false otherwise.
     */
    public boolean providesIdentifier();

    /**
     * Flag for indicating that an extension must be signed.
     *
     * @return  True if all the extension's parameters MUST be signed
     *          in positive assertions, or false if there isn't such a
     *          requirement.
     */
    public boolean signRequired();

}
