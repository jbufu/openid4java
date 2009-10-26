/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Vector;
import java.util.Iterator;

import org.openid4java.util.XmlConfigReader;
import org.openid4java.util.NameValuePair;
import org.openid4java.util.IdPValidator;
import org.openid4java.util.IdPValidatorConfig;

import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.ax.Attribute;
import org.openid4java.message.ParameterList;

import org.openid4java.discovery.Identifier;
import org.openid4java.discovery.DiscoveryInformation;

/**
 * @author Neill Miller, Rachana Ananthakrishnan
 */
public class AttributeProviderDriver
{
    private static Log _log = LogFactory.getLog(AttributeProviderDriver.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private Vector attrProviders = null;

    public Vector getAttributeProviders()
    {
        return this.attrProviders;
    }

    public void setAttributeProviders(Vector attrProviders)
    {
        _log.debug("setAttributeProviders called with " + attrProviders);
        this.attrProviders = attrProviders;
    }

    /*
      add all retrievable attributes for all configured attribute
      providers.  if the setAttributeProviders method was used to
      initialize a list of attrProviders before this method is called,
      the configurations are still checked each time this method is
      called and any valid attrProviders found are appended to the
      initial list and run in addition.
     */
    public void addAttributesToResponse(AuthSuccess response, String identifier, ParameterList pl)
    {
        _log.debug("addAttributesToResponse called on id: " + identifier);

        int i = 0;
        Vector curAttrProviders = new Vector();
        Vector attrProviderConfigs = null;
        AttributeProvider attrProvider = null;
        Iterator attrProviderIter = null;
        XmlConfigReader configReader = new XmlConfigReader();

        String attrProviderConfigFile = System.getenv("ATTR_PROVIDER_CONFIG_FILE");
        if (attrProviderConfigFile != null)
        {
            attrProviderConfigs = configReader.getAttrProviders(
                attrProviderConfigFile);

            if (attrProviderConfigs != null)
            {
                for(i = 0; i < attrProviderConfigs.size(); i++)
                {
                    AttributeProviderConfig attrProviderConfig =
                        (AttributeProviderConfig)attrProviderConfigs.get(i);
                    String className = attrProviderConfig.getClassName();
                    NameValuePair[] parameters = attrProviderConfig.getParameters();

                    try
                    {   
                        attrProvider = (AttributeProvider)
                            Class.forName(className).newInstance();
                        attrProvider.initialize(parameters);
                        curAttrProviders.add(attrProvider);
                    }
                    catch(Exception e)
                    {   
                        _log.error("Failed to initialize Attribute Provider: " + e);
                    }
                }
            }
        }

        if (this.attrProviders != null)
        {
            attrProviderIter = this.attrProviders.iterator();
            while(attrProviderIter.hasNext())
            {
                try
                {
                    attrProvider = (AttributeProvider)attrProviderIter.next();
                    curAttrProviders.add(attrProvider);
                }
                catch(Exception e)
                {
                    _log.error(
                        "Failed to add initialized attribute providers: " + e);
                }
            }
        }

        _log.info(curAttrProviders.size() + " Attribute Providers initialized!");

        // retrieve the attributes from the configured attribute providers
        if (curAttrProviders.size() > 0)
        {
            FetchResponse fetchResp = FetchResponse.createFetchResponse();
            Attribute[] attributes = null;
            attrProviderIter = curAttrProviders.iterator();
            while(attrProviderIter.hasNext())
            {
                attrProvider = (AttributeProvider)attrProviderIter.next();
                try
                {
                    attributes = attrProvider.getAttributes(identifier, pl);

                    // add each attribute to the auth response
                    for(i = 0; i < attributes.length; i++)
                    {
                        List tmp = attributes[i].getValues();
                        if (tmp != null)
                        {
                            for(int j = 0; j < tmp.size(); j++)
                            {
                                String curValue = (String)attributes[i].getValues().get(j);

                                fetchResp.addAttribute(
                                    attributes[i].getAlias(),
                                    attributes[i].getType(),
                                    curValue);

                                if (DEBUG) _log.debug(
                                    "Added attribute " + attributes[i].getAlias() +
                                    " = " + curValue);
                            }
                        }
                        else
                        {
                            if (DEBUG) _log.debug("Attribute " + attributes[i].getAlias() +
                                                  " has NULL value.  Skipping.");
                        }
                    }
                }
                catch(Exception e)
                {
                    _log.error("Failed to retrieve attributes from " +
                               "configured provider: " + e);
                }
            }

            try
            {
                // finally, add the fetch response to the auth response
                ((AuthSuccess)response).addExtension(fetchResp);
            }
            catch(Exception e)
            {
                _log.error("Failed to add attributes in " +
                           "extension to response: " + e);
            }
        }
    }
}