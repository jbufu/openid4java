/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.util;

/**
 * Utility bean for setting transport properties in runtime.
 */

public class ProxyProperties
{
    protected int proxyPort = -1;
    protected String domain;
    protected String password;
    protected String proxyHostName;
    protected String userName;

    public ProxyProperties()
    {
    }

    public String getDomain()
    {
        if (domain == null || domain.length() == 0)
        {
            return "anonymous";
        }
        else
        {
            return domain;
        }
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    public String getPassword()
    {
        if (password == null || password.length() == 0)
        {
            return "anonymous";
        }
        else
        {
            return password;
        }
    }

    public void setPassword(String passWord)
    {
        this.password = passWord;
    }

    public String getProxyHostName()
    {
        return proxyHostName;
    }

    public void setProxyHostName(String proxyHostName)
    {
        this.proxyHostName = proxyHostName;
    }

    public int getProxyPort()
    {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort)
    {
        this.proxyPort = proxyPort;
    }

    public String getUserName()
    {
        if (userName == null || userName.length() == 0)
        {
            return "anonymous";
        }
        else
        {
            return userName;
        }
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }
}

