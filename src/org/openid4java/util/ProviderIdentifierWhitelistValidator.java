/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.util;

import java.net.URL;

import java.util.HashMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;

import org.openid4java.util.NameValuePair;
import org.openid4java.util.ConfigException;
import org.openid4java.util.IdPInvalidException;

/**
 * @author Neill Miller, Rachana Ananthakrishnan
 */
public class ProviderIdentifierWhitelistValidator implements IdPValidator
{
    private HashMap policy = null;
    private static String CONFIG_FILE_PARAM = "config-file";

    public void initialize(NameValuePair[] parameters)
        throws ConfigException
    {
        String filename = null;
        for(int i = 0; i < parameters.length; i++)
        {
            if (parameters[i].getName().equals(CONFIG_FILE_PARAM))
            {
                filename = parameters[i].getValue();
            }
        }

        try
        {
            FileInputStream fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(
                new InputStreamReader(in));
            String line = null, endpoint = null, policyIdentity = null;

            this.policy = new HashMap();

            while((line = br.readLine()) != null)
            {
                if (line.charAt(0) == '#')
                {
                    continue;
                }
                int pos = line.indexOf(",");
                if (pos != -1)
                {
                    endpoint = line.substring(0, pos);
                    endpoint = endpoint.trim();

                    policyIdentity = line.substring(pos + 1);
                    policyIdentity = policyIdentity.trim();

                    this.policy.put((Object)endpoint, (Object)policyIdentity);
                }
            }
            in.close();
        }
        catch (Exception e)
        {
            throw new ConfigException(
                "Failed to parse ProviderIdentifierWhitelist config" + e);
        }
    }

    /*
      this validate method returns true if the endpoint specified is
      the configured endpoint for this validator that matches the
      identity provided
     */
    public void validate(URL idpEndpoint, String idpIdentity)
        throws IdPInvalidException, ConfigException
    {
        if (this.policy == null)
        {
            throw new ConfigException("Uninitialized policy in validate");
        }

        String policyIdentity = (String)
            this.policy.get((Object)idpEndpoint.toString());
        if (policyIdentity == null)
        {
            throw new IdPInvalidException("Identity is unmapped");
        }

        if (!policyIdentity.equals(idpIdentity))
        {
            throw new IdPInvalidException("Endpoint found/Identity mismatched");
        }
    }
}
