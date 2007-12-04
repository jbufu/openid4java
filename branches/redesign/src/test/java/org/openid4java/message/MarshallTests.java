
package org.openid4java.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openid4java.message.Marshaller;
import org.openid4java.message.Unmarshaller;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.ax.impl.FetchResponseImpl;
import org.openid4java.message.ax.impl.FetchResponseMarshaller;
import org.openid4java.message.encoding.MessageCodec;
import org.openid4java.message.encoding.impl.KeyValueFormCodec;
import org.openid4java.message.encoding.impl.URLFormCodec;
import org.openid4java.message.sreg.SimpleRegistration;
import org.openid4java.message.sreg.SimpleRegistrationRequest;
import org.openid4java.message.sreg.SimpleRegistration.Field;
import org.openid4java.message.sreg.impl.SimpleRegistrationRequestMarshaller;
import org.openid4java.message.sreg.impl.SimpleRegistrationRequestUnmarshaller;

import junit.framework.TestCase;


/**
 * Marshall Tests.
 */
public class MarshallTests extends TestCase {

    /**
     * Test simple registration marshalling.
     */
    public void testSimpleRegistration() {
        String queryString = "required=fullname,email&optional=gender,country,height";
        MessageCodec codec = new URLFormCodec();
        Unmarshaller<SimpleRegistrationRequest> unmarshaller = new SimpleRegistrationRequestUnmarshaller();
        Marshaller<SimpleRegistrationRequest> marshaller = new SimpleRegistrationRequestMarshaller();

        // build request
        SimpleRegistrationRequest request = unmarshaller.unmarshall(codec.decode(queryString));

        // check assertions
        assertEquals(SimpleRegistration.SREG_11_NS, request.getNamespace());

        assertEquals(2, request.getRequiredFields().size());
        assertTrue(request.getRequiredFields().contains(Field.valueOf("fullname")));
        assertTrue(request.getRequiredFields().contains(Field.valueOf("email")));

        assertEquals(2, request.getOptionalFields().size());
        assertTrue(request.getOptionalFields().contains(Field.valueOf("gender")));
        assertTrue(request.getOptionalFields().contains(Field.valueOf("country")));
        try {
            assertFalse(request.getOptionalFields().contains(Field.valueOf("height")));
        } catch (IllegalArgumentException e) {
            // do nothing - expected
        }

        //assertEquals(queryString, codec.encode(marshaller.marshall(request)));

    }

    /**
     * Test attribute exchange marshalling.
     */
    public void testAttributeExchange() {
        FetchResponseImpl response = new FetchResponseImpl();
        Marshaller<FetchResponse> marshaller = new FetchResponseMarshaller();
        MessageCodec codec = new KeyValueFormCodec();

        List<String> names = Arrays.asList(new String[] { "George Burdell" });
        response.getAttributes().put("http://axschema.org/namePerson/friendly", names);

        List<String> emails = new ArrayList<String>();
        emails.add("george@burdell.name");
        emails.add("gpburdell@gmail.com");

        response.getAttributes().put("http://axschema.org/contact/email", emails);

        System.out.println(codec.encode(marshaller.marshall(response)));
    }

}