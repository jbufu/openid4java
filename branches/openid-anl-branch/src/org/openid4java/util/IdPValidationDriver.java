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

    private Vector IdPValidators = null;

    public Vector getIdPValidators()
    {
        return this.IdPValidators;
    }

    public void setIdPValidators(Vector IdPValidators)
    {
        _log.debug("setIdPValidators called with " + IdPValidators);
        this.IdPValidators = IdPValidators;
    }

    /*
      perform all IdPValidation for all configured IdPValidators.  if
      the setIdPValidator method was used to initialize a list of
      IdPValidators before this method is called, the configurations
      are still checked each time this method is called and any valid
      IdPValidators found are appended to the initial list and run in
      addition.
     */
    public List performIdPValidation(Identifier identifier, List discoveries)
        throws IdPInvalidException
    {
        _log.debug("performIdPValidation called on id: " + identifier);

        int i = 0;
        Vector validators = new Vector();
        Vector validatorConfigs = null; 
        IdPValidator validator = null;
        Iterator validatorIter = null;
        XmlConfigReader configReader = new XmlConfigReader();

        String idpConfigFile = System.getenv("IDP_CONFIG_FILE");
        if (idpConfigFile != null)
        {
            validatorConfigs = configReader.getValidators(idpConfigFile);

            if (validatorConfigs != null)
            {
                for(i = 0; i < validatorConfigs.size(); i++)
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
            }
        }

        if (this.IdPValidators != null)
        {
            validatorIter = this.IdPValidators.iterator();
            while(validatorIter.hasNext())
            {
                try
                {
                    validator = (IdPValidator)validatorIter.next();
                    validators.add(validator);
                }
                catch(Exception e)
                {
                    _log.error("Failed to add initialized validators: " + e);
                }
            }
        }

        _log.info(validators.size() + " IdPValidators initialized!");

        // validate the discovered endpoints
        if (validators.size() > 0)
        {
            int count = 0;
            List newDiscoveries = new ArrayList();
            validatorIter = validators.iterator();
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
                            "Whitelist Validator[" + count + "] Accepting " +
                            "endpoint: " + dInfo.getOPEndpoint());

                        newDiscoveries.add((Object)dInfo);
                        count++;
                    }
                    catch(Exception e)
                    {
                        _log.info(
                            "Whitelist Validator[" + count + "] Rejecting " +
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
        return discoveries;
    }
}
