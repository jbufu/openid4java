/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.message;

import net.openid.message.ax.AxMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of the OpenID extension for which there is an implementation
 * available.
 *
 * @see MessageExtension Message
 * @author Marius Scurtescu, Johnny Bufu
 */
public class MessageExtensionFactory
{
    /**
     * Map with associations of Type URIs to class objects of the implementation
     */
    private static Map _extensions = new HashMap(5);
    static
    {
        _extensions.put(AxMessage.OPENID_NS_AX, AxMessage.class);
    }

    /**
     * Adds a new extension implementation to the extension factory.
     *
     * @param typeUri       The URI that identifies the extension.
     * @param clazz         The implementation class for the extension.
     */
    public static void addExtension(String typeUri, Class clazz)
    {
        _extensions.put(typeUri, clazz);
    }

    /**
     * Returns true if there is an implementation available for extension
     * identified by the specified Type URI, or false otherwise.
     *
     * @param typeUri   The Type URI that identifies a extension.
     */
    public static boolean hasExtension(String typeUri)
    {
        return _extensions.containsKey(typeUri);
    }

    /**
     * Returns true if there is an implementation available for extension
     * identified by the specified Type URI and the extension provides
     * authentication services, or false otherwise.
     *
     * @param typeUri   The Type URI that identifies a extension.
     */
    public static boolean providesIdentifier(String typeUri)
    {
        MessageExtension ext = getExtension(typeUri);

        return ext != null && ext.providesIdentifier();
    }

    /**
     * Gets a MessageExtension for the specified Type URI
     * if an implementation is available, or null otherwise.
     * <p>
     * The returned object has an empty parameter list.
     *
     * @param typeUri   The Type URI that identifies a extension.
     * @see             MessageExtension Message
     */
    public static MessageExtension getExtension(String typeUri)
    {
        if (! hasExtension(typeUri))
            return null;

        MessageExtension extension;

        try
        {
            Class extensionClass = (Class) _extensions.get(typeUri);
            extension = (MessageExtension) extensionClass.newInstance();
        }
        catch (Exception e)
        {
            return null;
        }

        return extension;
    }
}
