/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.util;

import java.net.URL;

import java.util.List;
import java.util.ArrayList;

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
public class ProviderWhitelistValidator implements IdPValidator
{
    private List policy = null;
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
            String line = null;
            FileInputStream fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(
                new InputStreamReader(in));

            this.policy = new ArrayList();

            while((line = br.readLine()) != null)
            {
                if (line.charAt(0) == '#')
                {
                    continue;
                }
                this.policy.add((Object)line);
            }
            in.close();
        }
        catch (Exception e)
        {
            throw new ConfigException(
                "Failed to parse ProviderWhitelist config" + e);
        }
    }

    /*
      this validate method returns true if the endpoint specified is
      found in the configuration file of acceptable idp endpoints,
      regardless of what the identity is
     */
    public void validate(URL idpEndpoint, String idpIdentity)
        throws IdPInvalidException, ConfigException
    {
        if (this.policy == null)
        {
            throw new ConfigException("Uninitialized policy in validate");
        }

        if (!this.policy.contains((Object)idpEndpoint.toString()))
        {
            throw new IdPInvalidException(
                "Provider " + idpEndpoint.toString() + " is unrecognized");
        }
    }
}
