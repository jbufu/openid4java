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

import org.openid4java.discovery.Identifier;
import org.openid4java.discovery.DiscoveryInformation;

/**
 * @author Neill Miller, Rachana Ananthakrishnan
 */
public class IdPValidationDriver
{
    private static Log _log = LogFactory.getLog(IdPValidationDriver.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    public static List performIdPValidation(Identifier identifier, List discoveries)
        throws IdPInvalidException
    {
        // initialize any idp validators, if any are configured
        String idpConfigFile = System.getenv("IDP_CONFIG_FILE");
        if (idpConfigFile != null)
        {
            XmlConfigReader configReader = new XmlConfigReader();
            Vector validatorConfigs = configReader.getValidators(idpConfigFile);
            Vector validators = new Vector();
            IdPValidator validator = null;

            for(int i = 0; i < validatorConfigs.size(); i++)
            {
                IdPValidatorConfig idpConfig = (IdPValidatorConfig)validatorConfigs.get(i);
                String className = idpConfig.getClassName();
                NameValuePair[] parameters = idpConfig.getParameters();

                try
                {
                    validator = (IdPValidator)Class.forName(className).newInstance();
                    validator.initialize(parameters);
                    validators.add(validator);
                }
                catch(Exception e)
                {
                    _log.error("Failed to initialize validator: " + e);
                }
            }

            _log.info(validators.size() + " IdPValidators initialized!");

            // validate the discovered endpoints
            if (validators.size() > 0)
            {
                List newDiscoveries = new ArrayList();
                Iterator validatorIter = validators.iterator();
                while(validatorIter.hasNext())
                {
                    validator = (IdPValidator)validatorIter.next();

                    Iterator iter = discoveries.iterator();
                    while(iter.hasNext())
                    {
                        DiscoveryInformation dInfo = (DiscoveryInformation)iter.next();
                        try
                        {
                            validator.validate(
                                dInfo.getOPEndpoint(), identifier.getIdentifier());

                            _log.info(
                                "Whitelist Validator Accepting " +
                                "endpoint: " + dInfo.getOPEndpoint());

                            newDiscoveries.add((Object)dInfo);
                        }
                        catch(Exception e)
                        {
                            _log.info(
                                "Whitelist Validator rejecting " +
                                "endpoint: " + dInfo.getOPEndpoint() +
                                ": " + e);
                        }
                    }
                }

                if (newDiscoveries.size() > 0)
                {
                    discoveries = newDiscoveries;
                    _log.info("Found " + discoveries.size() +
                              " valid endpoints.");
                }
                else
                {
                    discoveries = null;
                    _log.info("No valid endpoints were found " +
                              "after validation.");
                    throw new IdPInvalidException(
                        "No valid endpoints were found after validation");
                }
            }
        }
        return discoveries;
    }
}
