/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;

/**
 * Load properties from classpath:<code>org.openid4java.util.openid4java-default.properties</code>,
 * then load custom properties from classpath:<code>openid4java.properties</code>
 * to replace the default if exists..
 * 
 * @author Sutra Zhou
 * 
 */
public class OpenID4JavaUtils
{
    private static Log _log = LogFactory.getLog(OpenID4JavaUtils.class);

    private static final Properties _appProperties;

    static
    {
        // Load default properties first, then use custom properties to replace
        // the default.
        _appProperties = new Properties();
        _appProperties.putAll(loadProperties("openid4java-default.properties"));
        Properties custom = loadProperties("/openid4java.properties");
        if (custom != null)
        {
            _appProperties.putAll(custom);
        }
    }

    private static Properties loadProperties(String name)
    {
        Properties p = null;
        InputStream is = OpenIDException.class.getResourceAsStream(name);
        if (is != null)
        {
            p = new Properties();
            try
            {
                p.load(is);
            }
            catch (IOException e)
            {
                _log.error("Load properties from " + name + " failed.", e);
            }
            finally
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    _log.warn("Error closing resource stream.", e);
                }
            }
        }
        else
        {
            _log.debug("Resource " + name + " not found.");
        }
        return p;
    }

    public static String getProperty(String key)
    {
        return _appProperties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue)
    {
        return _appProperties.getProperty(key, defaultValue);
    }

    private OpenID4JavaUtils()
    {
    }
}
