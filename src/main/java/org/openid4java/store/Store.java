
package org.openid4java.store;

import java.net.URL;

import org.openid4java.association.Association;


public interface Store {

    public void generate(String type, int expiration);

    public void storeAssociation(URL serverURL, Association association);

    public Association getAssociation(URL serverURL, String handle);

    public Association getAssociation(URL serverURL);

    public Association getAssociation(String handle);

    public void removeAssociation(URL serverURL, String handle);

    public void removeAssociation(String handle);

}