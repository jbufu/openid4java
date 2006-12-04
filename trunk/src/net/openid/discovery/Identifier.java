/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.discovery;

import java.io.Serializable;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public interface Identifier extends Serializable
{
    public String getIdentifier();
}
