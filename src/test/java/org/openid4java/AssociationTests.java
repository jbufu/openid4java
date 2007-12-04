
package org.openid4java;

import junit.framework.TestCase;

import org.openid4java.association.Association.AssociationType;
import org.openid4java.association.Association.SessionType;
import org.openid4java.message.sreg.SimpleRegistration.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AssociationTests extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(AssociationTests.class);

    public void testAssociation() {
        log.info(Field.valueOf("nickname").toString());
        log.info(AssociationType.getType("HMAC-SHA1").toString());
        log.info(SessionType.getType("no-encryption").toString());
    }
}