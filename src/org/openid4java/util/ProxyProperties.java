/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.util;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;

/**
 * Utility bean for setting transport properties in runtime.
 */

public class ProxyProperties
{
    private static final String ANONYMOUS = "anonymous";

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
            return ANONYMOUS;
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
            return ANONYMOUS;
        }
        else
        {
            return password;
        }
    }

    public void setPassword(String password)
    {
        this.password = password;
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
            return ANONYMOUS;
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

    /**
	 * Get the proxy credentials.
	 * 
	 * @return the proxy credentials
	 */
    public Credentials getCredentials() {
        Credentials credentials = null;
        if (this.getDomain().equals(ANONYMOUS))
        {
            credentials = new UsernamePasswordCredentials(
                    this.getUserName(),
                    this.getPassword());
        }
        else
        {
            credentials = new NTCredentials(
                    this.getUserName(),
                    this.getPassword(),
                    this.getProxyHostName(),
                    this.getDomain());
        }
        return credentials;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.getDomain() + "\\" + this.getUserName()
            + ":" + this.getPassword()
            + "@" + this.getProxyHostName() + ":" + this.getProxyPort();
    }
}

