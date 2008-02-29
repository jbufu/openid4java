/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.infocard.sts;

/**
 * Implements the IConfigurableComponentFactory that creates the IssueHandler implementation.
 * 
 * @author Johnny Bufu
 */
public class OpenIDTokenGeneratorHandlerFactory
	implements org.eclipse.higgins.configuration.api.IConfigurableComponentFactory
{
	/**
	 * Provides access to the singleton instance
	 * 
	 * @return the singleton instance
	 */
	public org.eclipse.higgins.configuration.api.IConfigurableComponent getSingletonInstance()
	{
		return null;
	}
	
	/**
	 * Provides access to the new instance
	 * 
	 * @return the new instance
	 */
	public org.eclipse.higgins.configuration.api.IConfigurableComponent getNewInstance()
	{
		return 	new org.openid4java.infocard.sts.OpenIDTokenGeneratorHandler();
	}
}
