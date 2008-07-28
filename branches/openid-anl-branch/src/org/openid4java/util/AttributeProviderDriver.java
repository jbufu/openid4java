/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;

import org.openid4java.util.XmlConfigReader;
import org.openid4java.util.NameValuePair;
import org.openid4java.util.IdPValidator;
import org.openid4java.util.IdPValidatorConfig;

import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ax.FetchResponse;

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
    public void addAttributesToResponse(AuthSuccess response, String identifier)
    {
        // initialize any Attribute Providers, if any are configured
        String attrProviderConfigFile = System.getenv("ATTR_PROVIDER_CONFIG_FILE");
        if (attrProviderConfigFile != null)
        {
            int i = 0;
            XmlConfigReader configReader = new XmlConfigReader();
            Vector attrProviderConfigs = configReader.getAttrProviders(attrProviderConfigFile);
            Vector curAttrProviders = new Vector();
            AttributeProvider attrProvider = null;

            if (this.attrProviders != null)
            {
                curAttrProviders.add(this.attrProviders);
            }

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

            _log.info(curAttrProviders.size() + " Attribute Providers initialized!");

            // retrieve the attributes from the configured attribute providers
            if (curAttrProviders.size() > 0)
            {
                FetchResponse fetchResp = FetchResponse.createFetchResponse();
                NameValuePair[] attributes = null;
                Iterator attrProviderIter = curAttrProviders.iterator();
                while(attrProviderIter.hasNext())
                {
                    attrProvider = (AttributeProvider)attrProviderIter.next();
                    try
                    {
                        attributes = attrProvider.getAttributes(identifier);

                        // add each attribute to the auth response
                        for(i = 0; i < attributes.length; i++)
                        {
                            fetchResp.addAttribute(
                                attributes[i].getName(),
                                "http://schema.mcs.anl.gov/esg/attribute",
                                attributes[i].getValue());

                            if (DEBUG) _log.debug(
                                "Added attribute " + attributes[i].getName() +
                                " = " + attributes[i].getValue());
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
}