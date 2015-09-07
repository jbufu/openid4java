package org.openid4java.message.ax;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openid4java.message.MessageException;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;

public class FetchResponseTest
{
    @Test
    public void unlimitedCountWorksForLists() throws MessageException
    {
        ParameterList params = new ParameterList();
        params.set(new Parameter("required", "key"));
        params.set(new Parameter("count.key", "unlimited"));

        FetchRequest req = new FetchRequest(params);

        Map<String, List<String>> userData = Collections.singletonMap("key", Collections.singletonList("value"));

        FetchResponse response = FetchResponse.createFetchResponse(req, userData);

        assertEquals(Collections.singletonList("value"),
                response.getAttributeValues("key"));
    }
}
