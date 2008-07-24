/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.util;

import java.net.URL;

import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;

import org.openid4java.util.NameValuePair;
import org.openid4java.util.ConfigException;
import org.openid4java.util.AttributeProviderException;

/**
 * @author Neill Miller, Rachana Ananthakrishnan
 */
public class SQLAttributeProvider implements AttributeProvider
{
    private String CONFIG_FILE_PARAM = "config-file";
    private String dbHost = null, dbUser = null, dbPass = null;
    private String dbName = null, dbTableName = null;

    private Connection conn = null;

    private List attributeList = null;
    private NameValuePair[] attributes = null;

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
            String line = null, name = null, value = null;

            this.attributeList = new ArrayList();

            while((line = br.readLine()) != null)
            {
                if (line.charAt(0) == '#')
                {
                    continue;
                }
                int pos = line.indexOf("=");
                if (pos != -1)
                {
                    name = line.substring(0, pos);
                    name = name.trim();

                    value = line.substring(pos + 1);
                    value = value.trim();

                    if (name.toLowerCase().equals("host"))
                    {
                        this.dbHost = value;
                    }
                    else if (name.toLowerCase().equals("username"))
                    {
                        this.dbUser = value;
                    }
                    else if (name.toLowerCase().equals("password"))
                    {
                        this.dbPass = value;
                    }
                    else if (name.toLowerCase().equals("db"))
                    {
                        this.dbName = value;
                    }
                    else if (name.toLowerCase().equals("table"))
                    {
                        this.dbTableName = value;
                    }
                }
            }
            in.close();
        }
        catch (Exception e)
        {
            throw new ConfigException("Failed to parse SQLAttributeProvider config" + e);
        }
    }

    public NameValuePair[] getAttributes(String idpIdentity)
        throws AttributeProviderException, ConfigException
    {
        if ((this.dbHost == null) || (this.dbUser == null) ||
            (this.dbPass == null) || (this.dbName == null) ||
            (this.dbTableName == null))
        {
            throw new ConfigException("Uninitialized database settings in getAttributes");
        }

        if (this.conn == null)
        {
            String url = "jdbc:mysql://" + this.dbHost + "/" + this.dbName;
            System.out.println("Attempting connection to " + url);
            try
            {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                this.conn = DriverManager.getConnection(url, this.dbUser, this.dbPass);
            }
            catch(Exception e)
            {
                throw new AttributeProviderException(
                    "Cannot connect to " + url + ": " + e);
            }
        }

        int i = 0;
        String query = "SELECT attrName, attrValue ";
        query += "FROM " + this.dbTableName;
        query += " WHERE identity = '" + idpIdentity + "'";

        try
        {
            Statement statement = this.conn.createStatement();
            ResultSet rs = statement.executeQuery(query);

            this.attributeList = new ArrayList();
            while(rs.next())
            {
                this.attributeList.add(rs.getString("attrName"));
            }

            this.attributes = new NameValuePair[this.attributeList.size()];

            i = 0;
            rs.beforeFirst();

            for (Iterator iter = this.attributeList.iterator(); iter.hasNext(); i++)
            {
                rs.next();
                this.attributes[i] = new NameValuePair(
                    (String)iter.next(), rs.getString("attrValue"));
            }
        }
        catch(Exception e)
        {
            throw new AttributeProviderException(
                "Query " + query + "failed: " + e);
        }
        return this.attributes;
    }
}
