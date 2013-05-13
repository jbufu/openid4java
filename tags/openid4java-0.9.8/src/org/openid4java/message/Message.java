/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.pape.PapeMessage;
import org.openid4java.message.sreg.SReg11ExtensionFactory;
import org.openid4java.message.sreg.SRegMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class Message
{
    private static Log _log = LogFactory.getLog(Message.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    // message constants
    public static final String MODE_IDRES = "id_res";
    public static final String MODE_CANCEL = "cancel";
    public static final String MODE_SETUP_NEEDED = "setup_needed";
    public static final String OPENID2_NS = "http://specs.openid.net/auth/2.0";

    private ParameterList _params;
    private int _extCounter;

    // extention type URI -> extension alias : extension present in the message
    private Map _extAliases;

    // extension type URI -> MessageExtensions : extracted extension objects
    private Map _extesion;

    // the URL where this message should be sent, where applicable
    // should remain null for received messages (created from param lists)
    protected String _destinationUrl;

    // type URI -> message extension factory : supported extensions
    private static Map _extensionFactories = new HashMap();

    static
    {
        _extensionFactories.put(AxMessage.OPENID_NS_AX, AxMessage.class);
        _extensionFactories.put(SRegMessage.OPENID_NS_SREG, SRegMessage.class);
        _extensionFactories.put(SRegMessage.OPENID_NS_SREG11, SReg11ExtensionFactory.class);
        _extensionFactories.put(PapeMessage.OPENID_NS_PAPE, PapeMessage.class);
    }

    protected Message()
    {
        _params = new ParameterList();
        _extCounter = 0;
        _extAliases = new HashMap();
        _extesion   = new HashMap();
    }

    protected Message (ParameterList params)
    {
        this();
        this._params = params;

        //build the extension list when creating a message from a param list
        Iterator iter = _params.getParameters().iterator();

        // simple registration is a special case; we support only:
        // SREG1.0 (no namespace, "sreg" alias hardcoded) in :
        //   - OpenID1 messages
        //   - OpenID2 messages (against the 2.0 spec),
        //     to accomodate Yahoo's non-2.0-compliant implementation
        // SREG1.1 (namespace, any possible alias) in OpenID2 messages
        boolean hasOpenidDotSreg = false;

        while (iter.hasNext())
        {
            String key = ((Parameter) iter.next()).getKey();
            if (key.startsWith("openid.ns.") && key.length() > 10)
                _extAliases.put(_params.getParameter(key).getValue(),
                        key.substring(10));

            if (key.startsWith("openid.sreg."))
                hasOpenidDotSreg = true;
        }

        // only do the workaround for OpenID1 messages
        if ( hasOpenidDotSreg && ! _extAliases.values().contains("sreg")
             /*! todo: revert this: hasParameter("openid.ns")*/ )
            _extAliases.put(SRegMessage.OPENID_NS_SREG, "sreg");

        _extCounter = _extAliases.size();
    }

    public static Message createMessage() throws MessageException
    {
        Message message = new Message();

        message.validate();

        if (DEBUG) _log.debug("Created message:\n"
                              + message.keyValueFormEncoding());

        return message;
    }

    public static Message createMessage(ParameterList params)
            throws MessageException
    {
        Message message = new Message(params);

        message.validate();

        if (DEBUG) _log.debug("Created message from parameter list:\n"
                              + message.keyValueFormEncoding());

        return message;
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
        Map params = new LinkedHashMap();

        Iterator iter = _params.getParameters().iterator();
        while (iter.hasNext())
        {
            Parameter p = (Parameter) iter.next();
            params.put( p.getKey(), p.getValue() );
        }

        return params;
    }

    /**
     * Checks that all required parameters are present
     */
    public void validate() throws MessageException
    {
        List requiredFields = getRequiredFields();

        Iterator paramIter = _params.getParameters().iterator();
        while (paramIter.hasNext())
        {
            Parameter param = (Parameter) paramIter.next();
            if (!param.isValid())
                throw new MessageException("Invalid parameter: " + param);
        }

        if (requiredFields == null)
            return;

        Iterator reqIter = requiredFields.iterator();
        while(reqIter.hasNext())
        {
            String required = (String) reqIter.next();
            if (! hasParameter(required))
                throw new MessageException(
                    "Required parameter missing: " + required);
        }
    }

    public List getRequiredFields()
    {
        return null;
    }

    public String keyValueFormEncoding()
    {
        return _params.toString();
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

    /**
     * Gets the URL where the message should be sent, where applicable.
     * Null for received messages.
     *
     * @param   httpGet     If true, the wwwFormEncoding() is appended to the
     *                      destination URL; the return value should be used
     *                      with a GET-redirect.
     *                      If false, the verbatim destination URL is returned,
     *                      which should be used with a FORM POST redirect.
     *
     * @see #wwwFormEncoding()
     */
    public String getDestinationUrl(boolean httpGet)
    {
        if (_destinationUrl == null)
            throw new IllegalStateException("Destination URL not set; " +
                    "is this a received message?");

        if (httpGet)  // append wwwFormEncoding to the destination URL
        {
            boolean hasQuery = _destinationUrl.indexOf("?") > 0;
            String initialChar = hasQuery ? "&" : "?";

            return _destinationUrl + initialChar + wwwFormEncoding();
        }
        else  // should send the keyValueFormEncoding in POST data
            return _destinationUrl;
    }


    // ------------ extensions implementation ------------

    /**
     * Adds a new extension factory.
     *
     * @param clazz         The implementation class for the extension factory,
     *                      must implement {@link MessageExtensionFactory}.
     */
    public static void addExtensionFactory(Class clazz) throws MessageException
    {
        try
        {
            MessageExtensionFactory extensionFactory =
                    (MessageExtensionFactory) clazz.newInstance();

            if (DEBUG) _log.debug("Adding extension factory for " +
                                  extensionFactory.getTypeUri());

            _extensionFactories.put(extensionFactory.getTypeUri(), clazz);
        }
        catch (Exception e)
        {
            throw new MessageException(
                    "Cannot instantiante message extension factory class: " +
                            clazz.getName());
        }
    }

    /**
     * Returns true if there is an extension factory available for extension
     * identified by the specified Type URI, or false otherwise.
     *
     * @param typeUri   The Type URI that identifies an extension.
     */
    public static boolean hasExtensionFactory(String typeUri)
    {
        return _extensionFactories.containsKey(typeUri);
    }

    /**
     * Gets a MessageExtensionFactory for the specified Type URI
     * if an implementation is available, or null otherwise.
     *
     * @param typeUri   The Type URI that identifies a extension.
     * @see             MessageExtensionFactory Message
     */
    public static MessageExtensionFactory getExtensionFactory(String typeUri)
    {
        if (! hasExtensionFactory(typeUri))
            return null;

        MessageExtensionFactory extensionFactory;

        try
        {
            Class extensionClass = (Class) _extensionFactories.get(typeUri);
            extensionFactory = (MessageExtensionFactory) extensionClass.newInstance();
        }
        catch (Exception e)
        {
            _log.error("Error getting extension factory for " + typeUri);
            return null;
        }

        return extensionFactory;
    }

    /**
     * Returns true if the message has parameters for the specified
     * extension type URI.
     *
     * @param typeUri       The URI that identifies the extension.
     */
    public boolean hasExtension(String typeUri)
    {
        return _extAliases.containsKey(typeUri);
    }

    /**
     * Gets a set of extension Type URIs that are present in the message.
     */
    public Set getExtensions()
    {
        return _extAliases.keySet();
    }

    /**
     * Retrieves the extension alias that will be used for the extension
     * identified by the supplied extension type URI.
     * <p>
     * If the message contains no parameters for the specified extension,
     * null will be returned.
     *
     * @param extensionTypeUri      The URI that identifies the extension
     * @return                      The extension alias associated with the
     *                              extension specifid by the Type URI
     */
    public String getExtensionAlias(String extensionTypeUri)
    {
        return (_extAliases.get(extensionTypeUri) != null) ?
                (String) _extAliases.get(extensionTypeUri) : null;
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
    public void addExtension(MessageExtension extension) throws MessageException
    {
       addExtension(extension, null);
    }

    /**
     * Adds a set of extension-specific parameters to a message,
     * trying to use the supplied preferredAlias
     * if the supplied alias is not already taken.
     *
     * Allows adding pseude-extensions to v1 messages
     * but only if the supplied preferredAlias can be used;
     * MessageException is thrown otherwise.
     *
     * @param extension the extension to be added to this message
     * @param preferredAlias the preferred alias to use for the extension's parameters
     * @return the actual alias that was used for the extension
     * @throws MessageException if the preferredAlias could not be used
     * and this is a v1 message
     */
    public String addExtension(MessageExtension extension, String preferredAlias) throws MessageException
    {
        String typeUri = extension.getTypeUri();

        if (hasExtension(typeUri))
            throw new MessageException("Extension already present: " + typeUri);

        String alias = preferredAlias != null && ! _extAliases.containsValue(preferredAlias) ?
                preferredAlias : "ext" + Integer.toString(++ _extCounter);

        if (! hasParameter("openid.ns") && preferredAlias != null && ! alias.equals(preferredAlias))
            throw new MessageException("Cannot add (pseudo) extension to v1 message for alias: " + preferredAlias);

        // use the hardcoded "sreg" alias for SREG, for seamless interoperation
        // between SREG10/OpenID1 and SREG11/OpenID2
        if (SRegMessage.OPENID_NS_SREG.equals(typeUri))
            alias = "sreg";

        _extAliases.put(typeUri, alias);

        if (DEBUG) _log.debug("Adding extension; type URI: "
                              + typeUri + " alias: " +alias);

        //if (hasParameter("openid.ns"))
            set("openid.ns." + alias, typeUri);

        Iterator iter = extension.getParameters().getParameters().iterator();
        while (iter.hasNext())
        {
            Parameter param = (Parameter) iter.next();

            String paramName = param.getKey().length() > 0 ?
                    "openid." + alias + "." + param.getKey() :
                    "openid." + alias;

            set(paramName, param.getValue());
        }


        if (this instanceof AuthSuccess)
        {
            if (extension.signRequired())
                ((AuthSuccess)this).addSignExtension(typeUri);

            if ( ((AuthSuccess)this).getSignExtensions().contains(typeUri) )
                ((AuthSuccess)this).buildSignedList();
        }

        return alias;
    }

    /**
     * Retrieves the parameters associated with a protocol extension,
     * specified by the given extension type URI.
     * <p>
     * The "openid.ns.<extension_alias>" parameter is NOT included in the
     * returned list. Also, the returned parameter names will have the
     * "openid.<extension_alias>." prefix removed.
     *
     * @param extensionTypeUri      The type URI that identifies the extension
     * @return                      A ParameterList with all parameters
     *                              associated with the specified extension
     */
    private ParameterList getExtensionParams(String extensionTypeUri)
    {
        ParameterList extension = new ParameterList();

        if (hasExtension(extensionTypeUri))
        {
            String extensionAlias = getExtensionAlias(extensionTypeUri);

            Iterator iter = getParameters().iterator();
            while (iter.hasNext())
            {
                Parameter param = (Parameter) iter.next();
                String paramName = null;

                if (param.getKey().startsWith("openid." + extensionAlias + "."))
                    paramName = param.getKey()
                            .substring(8 + extensionAlias.length());

                if (param.getKey().equals("openid." + extensionAlias))
                    paramName = "";

                if (paramName != null)
                    extension.set(new Parameter(paramName, param.getValue()));
            }
        }

        return extension;
    }

    /**
     * Gets a MessageExtension for the specified Type URI if an implementation
     * is available, or null otherwise.
     * <p>
     * The returned object will contain the parameters from the message
     * belonging to the specified extension.
     *
     * @param typeUri               The Type URI that identifies a extension.
     */
    public MessageExtension getExtension(String typeUri) throws MessageException
    {
        if (!_extesion.containsKey(typeUri))
        {
            if (hasExtensionFactory(typeUri))
            {
                MessageExtensionFactory extensionFactory = getExtensionFactory(typeUri);

                String mode = getParameterValue("openid.mode");

                MessageExtension extension = extensionFactory.getExtension(
                        getExtensionParams(typeUri), mode.startsWith("checkid_"));

                if (this instanceof AuthSuccess && extension.signRequired())
                {
                    List signedParams = Arrays.asList(
                        ((AuthSuccess)this).getSignList().split(",") );

                    String alias = getExtensionAlias(typeUri);

                    if ( hasParameter("openid.ns") && ! signedParams.contains("ns." + alias))
                        throw new MessageException("Namespace declaration for extension "
                                                    + typeUri + " MUST be signed");
                    Iterator iter = extension.getParameters().getParameters().iterator();
                    while (iter.hasNext())
                    {
                        Parameter param = (Parameter) iter.next();
                        if (! signedParams.contains(alias + "." + param.getKey()))
                        {
                            throw new MessageException(
                                "Extension " + typeUri + " MUST be signed; " +
                                "field " + param.getKey() + " is NOT signed.");
                        }
                    }
                }

                _extesion.put(typeUri, extension);
            }
            else
                throw new MessageException("Cannot instantiate extension: " + typeUri);
        }

        if (DEBUG) _log.debug("Extracting " + typeUri +" extension from message...");

        return (MessageExtension) _extesion.get(typeUri);
    }
}
