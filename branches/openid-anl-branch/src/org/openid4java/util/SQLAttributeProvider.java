/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.util;

import java.net.URL;

import java.util.List;
import java.util.Vector;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openid4java.message.ax.Attribute;

import org.openid4java.util.ConfigException;
import org.openid4java.util.AttributeProviderException;

/**
 * @author Neill Miller, Rachana Ananthakrishnan
 */
public class SQLAttributeProvider implements AttributeProvider
{
    private static Log _log = LogFactory.getLog(SQLAttributeProvider.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private String CONFIG_FILE_PARAM = "config-file";
    private String dbHost = null, dbUser = null, dbPass = null;
    private String dbName = null, dbTableName = null;

    private Connection conn = null;

    private Vector attributes = null;

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

            while((line = br.readLine()) != null)
            {
                if ((line.charAt(0) == '#') || (line.length() < 3))
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

    /*
      NOTE:
      This code assumes it's working with a MySQL DB and a table that
      was created in a manner similar to this:

      create table TABLENAME(identity VARCHAR(255), attrAlias VARCHAR(255),
                             attrType VARCHAR(255), attrValue VARCHAR(255));
    */
    public Attribute[] getAttributes(String idpIdentity)
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
            _log.info("Attempting connection to " + url);

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
        String query = "SELECT attrAlias, attrType, attrValue ";
        query += "FROM " + this.dbTableName;
        query += " WHERE identity = '" + idpIdentity + "'" ;
        query += " order by attrAlias";

        try
        {
            Statement statement = this.conn.createStatement();
            ResultSet rs = statement.executeQuery(query);

            this.attributes = new Vector();
            while(rs.next())
            {
                this.addAttribute(rs.getString("attrAlias"),
                                  rs.getString("attrType"),
                                  rs.getString("attrValue"));
            }
        }
        catch(Exception e)
        {
            throw new AttributeProviderException(
                "Query " + query + "failed: " + e);
        }

        Attribute[] attrArray = new Attribute[this.attributes.size()];
        return (Attribute[])this.attributes.toArray(attrArray);
    }

    private void addAttribute(String alias, String type, String value)
    {
        List l = null;
        boolean attrAdded = false;

        /* check first if we're adding a value to an existing attr */
        for(int i = 0; i < this.attributes.size(); i++)
        {
            Attribute tmp = (Attribute)this.attributes.get(i);
            if (tmp.getAlias().equals(alias) && tmp.getType().equals(type))
            {
                /* increment count to existing attr and add value to existing value list */
                tmp.setCount(tmp.getCount() + 1);

                l = tmp.getValues();
                l.add((Object)value);

                attrAdded = true;
                break;
            }
        }

        /* otherwise add this attr because it doesn't exist yet */
        if (attrAdded == false)
        {
            l = new ArrayList();
            l.add((Object)value);

            this.attributes.add((Object)new Attribute(alias, type, l));
        }
    }
}
