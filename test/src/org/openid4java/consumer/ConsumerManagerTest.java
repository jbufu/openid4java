package org.openid4java.consumer;

import junit.framework.TestCase;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.openid4java.association.AssociationSessionType;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AssociationResponse;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//import edu.emory.mathcs.backport.java.util.Collections;

public class ConsumerManagerTest extends TestCase {
	private MockOpenIDServer server;
	private ConsumerManager manager;
	
	public void setUp() throws Exception {
		manager = new ConsumerManager();
		server = MockOpenIDServer.createAndStart();
	}
	
	public void tearDown() throws Exception {
		if(server != null) {
			server.stop();
			server.join();
		}
	}

	public void testPerferredAssociation() throws Exception {
		manager.setPrefAssocSessEnc(AssociationSessionType.DH_SHA1);
		DiscoveryInformation disc = new DiscoveryInformation(new URL(server.createAbsoluteUrl("/op/endpoint")), null);
		DiscoveryInformation info = manager.associate(Collections.singletonList(disc));
		assertEquals(1,server.getRequestParams().size());
		Map request = (Map)server.getRequestParams().get(0);
		assertEquals(manager.getPrefAssocSessEnc().getAssociationType(),((String[])request.get("openid.assoc_type"))[0]);
		assertEquals(manager.getPrefAssocSessEnc().getSessionType(),((String[])request.get("openid.session_type"))[0]);
	}
	
	private static class MockOpenIDServer extends Server {

		private final int port;

		private List requestParams = new LinkedList();

		public MockOpenIDServer(int port) {
			super(port);
			this.port = port;
			setHandler(new AbstractHandler() {				
				public void handle(String target, HttpServletRequest request,
						HttpServletResponse response, int dispatch)
						throws IOException, ServletException {
					MockOpenIDServer.this.requestParams.add(request.getParameterMap());
					
					ParameterList params = new ParameterList();
					params.set(new Parameter("ns",AssociationResponse.OPENID2_NS));
					params.set(new Parameter("assoc_handle",String.valueOf(System.nanoTime())));
					params.set(new Parameter("assoc_type",request.getParameter("openid.assoc_type")));
					params.set(new Parameter("session_type",request.getParameter("openid.session_type")));			
					params.set(new Parameter("expires_in","1799"));
					params.set(new Parameter("dh_server_public","eRm/Qn9lXQJc30ZQLtNFkrjQHuQCLyQ2fRNwLZTGVP50Lhx16EjksA6N0RvXzoJgY8/FdKioOYXKeWVvstHTUReXfF5EC9cnTVOFtTrMegJXHZIHdk+IITwsfGfTlVxMOc7DdCFOOMRWMOA9sYB5n5OoxnzYCob3vo39+Xytlcs="));
					params.set(new Parameter("enc_mac_key","CY08gTx1u4XravtWT3V5Er4sG+o="));
					response.getWriter().write(params.toString());
		            ((Request) request).setHandled(true);	            
				}
			});
		}
		
		public String createAbsoluteUrl(String baseUrl) {
			return "http://localhost:"+getPort()+baseUrl;
		}

		public final int getPort() {
			return port;
		}

		/**
		 * A List<Map<String,String[]>> that contains each requests parameters.
		 * So if there are two requests made to this MockOpenIDServer, then the
		 * size of the list is 2 with the first entry being the
		 * HttpServletRequest.getParameterMap() of the first request and the
		 * second etry is the HttpServletRequest.getParameterMap() of the second
		 * request.
		 * 
		 * @return
		 */
		public final List getRequestParams() {
			return requestParams;
		}
		
		public static MockOpenIDServer createAndStart() throws Exception {
			ServerSocket socket = new ServerSocket(0);
			int port = socket.getLocalPort();
			socket.close();
			MockOpenIDServer result = new MockOpenIDServer(port);
			result.start();
			return result;
		}
	}
}
