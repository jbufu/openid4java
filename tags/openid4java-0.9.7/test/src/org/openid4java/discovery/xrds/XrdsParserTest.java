package org.openid4java.discovery.xrds;

import junit.framework.TestCase;
import org.openid4java.discovery.DiscoveryInformation;

/**
 * @author jbufu
 */
public class XrdsParserTest extends TestCase
{
    public void testXrdsParse() throws Exception
    {
        XrdsParser parser = new XrdsParserImpl();
        parser.parseXrds(XRD, DiscoveryInformation.OPENID_OP_TYPES);
    }


    public static final String XRD = "<XRDS xmlns=\"xri://$xrds\" ref=\"xri://(tel:+1-201-555-0123)*foo\">\n" +
        "\n" +
//                "<bla/>\n" +
        "    <XRD xmlns=\"xri://$xrd*($v*2.0)\" version=\"2.0\">\n" +
        "\n" +
        "        <Query>*foo</Query>\n" +
        "\n" +
        "        <Status code=\"100\"/>\n" +
        "\n" +
        "        <ServerStatus code=\"100\"/>\n" +
        "\n" +
        "        <Expires>2005-05-30T09:30:10Z</Expires>\n" +
        "\n" +
        "        <ProviderID>xri://(tel:+1-201-555-0123)</ProviderID>\n" +
        "\n" +
        "        <LocalID>*baz</LocalID>\n" +
        "\n" +
        "        <EquivID>https://example.com/example/resource/</EquivID>\n" +
        "\n" +
        "        <CanonicalID>xri://(tel:+1-201-555-0123)!1234</CanonicalID>\n" +
        "\n" +
        "        <CanonicalEquivID>\n" +
        "\n" +
        "         xri://=!4a76!c2f7!9033.78bd\n" +
        "\n" +
        "        </CanonicalEquivID>\n" +
        "\n" +
        "        <Service>\n" +
        "\n" +
        "            <ProviderID>\n" +
        "             xri://(tel:+1-201-555-0123)!1234\n" +
        "\n" +
        "            </ProviderID>\n" +
        "\n" +
        "            <Type>xri://$res*auth*($v*2.0)</Type>\n" +
        "\n" +
        "            <MediaType>application/xrds+xml</MediaType>\n" +
        "\n" +
        "            <URI priority=\"10\">http://resolve.example.com</URI>\n" +
        "\n" +
        "            <URI priority=\"15\">http://resolve2.example.com</URI>\n" +
        "\n" +
        "            <URI>https://resolve.example.com</URI>\n" +
        "\n" +
        "        </Service>\n" +
        "\n" +
        "        <Service>\n" +
        "\n" +
        "            <ProviderID>\n" +
        "             xri://(tel:+1-201-555-0123)!1234\n" +
        "\n" +
        "            </ProviderID>\n" +
        "\n" +
        "            <Type>xri://$res*auth*($v*2.0)</Type>\n" +
        "\n" +
        "            <MediaType>application/xrds+xml;https=true</MediaType>\n" +
        "\n" +
        "            <URI>https://resolve.example.com</URI>\n" +
        "\n" +
        "        </Service>\n" +
        "\n" +
        "        <Service>\n" +
        "\n" +
        "            <Type match=\"null\" />\n" +
        "\n" +
        "            <Path select=\"true\">/media/pictures</Path>\n" +
        "\n" +
        "            <MediaType select=\"true\">image/jpeg</MediaType>\n" +
        "\n" +
        "            <URI append=\"path\" >http://pictures.example.com</URI>\n" +
        "\n" +
        "        </Service>\n" +
        "\n" +
        "        <Service>\n" +
        "\n" +
        "            <Type match=\"null\" />\n" +
        "\n" +
        "            <Path select=\"true\">/media/videos</Path>\n" +
        "\n" +
        "            <MediaType select=\"true\">video/mpeg</MediaType>\n" +
        "\n" +
        "            <URI append=\"path\" >http://videos.example.com</URI>\n" +
        "\n" +
        "        </Service>\n" +
        "\n" +
        "        <Service>\n" +
        "\n" +
        "            <ProviderID> xri://!!1000!1234.5678</ProviderID>\n" +
        "\n" +
        "            <Type match=\"null\" />\n" +
        "\n" +
        "            <Path match=\"default\" />\n" +
        "\n" +
        "            <URI>http://example.com/local</URI>\n" +
        "\n" +
        "        </Service>\n" +
        "\n" +
        "        <Service>\n" +
        "\n" +
        "            <Type>http://example.com/some/service/v3.1</Type>\n" +
        "\n" +
        "            <URI>http://example.com/some/service/endpoint</URI>\n" +
        "\n" +
        "            <LocalID>https://example.com/example/resource/</LocalID>\n" +
        "\n" +
        "        </Service>\n" +
        "\n" +
        "    </XRD>\n" +
        "\n" +
        "</XRDS>";

}