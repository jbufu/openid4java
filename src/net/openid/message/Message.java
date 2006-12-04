/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.message;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.net.URLEncoder;
import java.net.URI;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class Message
{
    // message constants
    public static final String MODE_IDRES = "id_res";
    public static final String MODE_CANCEL = "cancel";
    public static final String OPENID2_NS = "http://openid.net/signon/2.0";

    private ParameterList _params;
    private int _extCounter;
    private Map _extAliases;


    public Message()
    {
        _params = new ParameterList();
        _extCounter = 0;
        _extAliases = new HashMap();
    }

    public Message (ParameterList params) throws MessageException
    {
        this._params = params;
        this._extAliases = new HashMap();

        //build the extension list when creating a message from a param list
        Iterator iter = _params.getParameters().iterator();
        while (iter.hasNext())
        {
            String key = ((Parameter) iter.next()).getKey();
            if (key.startsWith("openid.ns.") && key.length() > 10)
                _extAliases.put(_params.getParameter(key).getValue(),
                        key.substring(10));
        }
        _extCounter = _extAliases.size();

        if (! isValid()) throw new MessageException(
                "Invalid set of parameters for the requested message type");

    }

    protected Parameter getParameter(String name)
    {
        return _params.getParameter(name);
    }

    public String getParameterValue(String name)
    {
        return _params.getParameterValue(name);
    }

    public boolean hasParameter(String name)
    {
        return _params.hasParameter(name);
    }

    protected void set(String name, String value)
    {
        _params.set(new Parameter(name, value));
    }

    protected List getParameters()
    {
        return _params.getParameters();
    }


    public Map getParameterMap()
    {
        Map params = new HashMap();

        Iterator iter = _params.getParameters().iterator();
        while (iter.hasNext())
        {
            Parameter p = (Parameter) iter.next();
            params.put( p.getKey(), p.getValue() );
        }

        return params;
    }

    /**
     * Check that all required parameters are present
     */
    public boolean isValid()
    {
        List requiredFields = getRequiredFields();

        if (requiredFields == null)
            return true;

        Iterator reqIter = requiredFields.iterator();
        while(reqIter.hasNext())
        {
            String required = (String) reqIter.next();
            if (! hasParameter(required))
                return false;
        }

        return true;
    }

    public List getRequiredFields()
    {
        return null;
    }

    public String keyValueFormEncoding()
    {
        StringBuffer allParams = new StringBuffer("");

        List parameters = _params.getParameters();
        Iterator iterator = parameters.iterator();
        while (iterator.hasNext())
        {
            Parameter parameter = (Parameter) iterator.next();
            allParams.append(parameter.getKey());
            allParams.append(':');
            allParams.append(parameter.getValue());
            allParams.append('\n');
        }

        return allParams.toString();
    }

    public String wwwFormEncoding()
    {
        StringBuffer allParams = new StringBuffer("");

        List parameters = _params.getParameters();
        Iterator iterator = parameters.iterator();
        while (iterator.hasNext())
        {
            Parameter parameter = (Parameter) iterator.next();

            // All of the keys in the request message MUST be prefixed with "openid."
            if ( ! parameter.getKey().startsWith("openid."))
                allParams.append("openid.");

            try
            {
                allParams.append(URLEncoder.encode(parameter.getKey(), "UTF-8"));
                allParams.append('=');
                allParams.append(URLEncoder.encode(parameter.getValue(), "UTF-8"));
                allParams.append('&');
            }
            catch (UnsupportedEncodingException e)
            {
                return null;
            }
        }

        // remove the trailing '&'
        if (allParams.length() > 0)
            allParams.deleteCharAt(allParams.length() -1);

        return allParams.toString();
    }


    // ------------ extensions implementation ------------

    /**
     * Retrieves or generates the extension alias for the protocol extension
     * specified by the given URI string.
     * <p>
     * If the message doesn't contain already any parameters for the specified
     * extension, a new extension alias will be generated, making sure it will
     * not conflict with other existing extensions used in the message.
     *
     * @param extensionTypeUri      The URI that identifies the extension
     * @return                      The extension alias associated with the
     *                              extension specifid by the Type URI
     */
    private String getExtensionAlias(String extensionTypeUri)
    {
        String extensionAlias = (String) _extAliases.get(extensionTypeUri);

        if (extensionAlias == null)
        {
            extensionAlias = "ext" + new Integer(++ _extCounter);
            _extAliases.put(extensionTypeUri, extensionAlias);
        }

        return extensionAlias;
    }


    /**
     * Gets a set of extension Type URIs that are present in the message.
     */
    public Set getExtensions()
    {
        return _extAliases.keySet();
    }

    /**
     * Adds a set of extension-specific parameters to a message.
     * <p>
     * The parameter names must NOT contain the "openid.<extension_alias>"
     * prefix; it will be generated dynamically, ensuring there are no conflicts
     * between extensions.
     *
     * @param extension             A MessageExtension containing parameters
     *                              to be added to the message
     */
    public void addExtensionParams(MessageExtension extension)
    {
        URI typeUri = extension.getTypeUri();

        String alias = getExtensionAlias(typeUri.toString());

        set("openid.ns." + alias, typeUri.toString());

        Iterator iter = extension.getParameters().getParameters().iterator();
        while (iter.hasNext())
        {
            Parameter param = (Parameter) iter.next();

            String paramName = param.getKey().length() > 0 ?
                    "openid." + alias + "." + param.getKey() :
                    "openid." + alias;

            set(paramName, param.getValue());
        }
    }

    /**
     * Retrieves the parameters associated with a protocol extension,
     * specified by the given Type URI string.
     * <p>
     * The "openid.ns.<alias>" parameter is included in the returned list.
     *
     * @param extensionTypeUri      The Type URI that identifies the extension
     * @return                      A ParameterList with all parameters
     *                              associated with the specified extension
     */
    public ParameterList getExtensionParams(String extensionTypeUri)
    {
        ParameterList extension = new ParameterList();

        String extensionAlias = getExtensionAlias(extensionTypeUri);

        Iterator iter = getParameters().iterator();
        while (iter.hasNext())
        {
            Parameter param = (Parameter) iter.next();
            String paramName = null;

            if (param.getKey().startsWith("openid." + extensionAlias + "."))
                paramName = param.getKey().substring(8 + extensionAlias.length());

            if (param.getKey().equals("openid." + extensionAlias))
                paramName = "";

            if (paramName != null)
                extension.set(new Parameter(paramName, param.getValue()));
        }

        return extension;
    }

    /**
     * Returns true if the message has parameters for the specified typeUri.
     *
     * @param typeUri       The URI that identifies the extension.
     */
    public boolean hasExtension(String typeUri)
    {
        return _extAliases.containsKey(typeUri);
    }

    /**
     * Gets a MessageExtension for the specified Type URI
     * if an implementation is available, or null otherwise.
     * <p>
     * The returned object will contain the parameters from the message
     * belonging to the specified extension.
     *
     * @param typeUri               The Type URI that identifies a extension.
     */
    public MessageExtension getExtension(String typeUri)
    {
        MessageExtension extension = null;

        if (MessageExtensionFactory.hasExtension(typeUri))
        {
            extension = MessageExtensionFactory.getExtension(typeUri);
            extension.setParameters(getExtensionParams(typeUri));
        }

        return extension;
    }
}
