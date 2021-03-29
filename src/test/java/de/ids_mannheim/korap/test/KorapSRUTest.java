package de.ids_mannheim.korap.test;

import static org.junit.Assert.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.ClientResponse;

/**
 * The tests are based on the sample corpus from the Goethe corpus.
 * 
 * The tests require a running KustvaktServer.
 * Specify the Kustvakt service URI at
 * /KorapSRU/src/main/webapp/WEB-INF/web.xml
 * 
 * @author margaretha
 *
 */
public class KorapSRUTest extends KorapJerseyTest{
	private DocumentBuilder docBuilder;
	
	private ClientAndServer mockServer;
    private MockServerClient mockClient;
    
	@Before
    public void startMockServer () {
        mockServer = startClientAndServer(1080);
        mockClient = new MockServerClient("localhost", mockServer.getPort());
    }
	
	@After
    public void stopMockServer () {
        mockServer.stop();
    }
	
	public KorapSRUTest() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		docBuilder = factory.newDocumentBuilder();
	}
	
	@Test
	public void searchRetrieveCQLTest() throws IOException, SAXException{
	    ClientResponse response = resource().queryParam("operation", "searchRetrieve")
                .queryParam("query", "fein")
                .get(ClientResponse.class);

	    InputStream entity = response.getEntity(InputStream.class);
        checkSRUSearchRetrieveResponse(entity);
	}
	
	@Test
    public void searchRetrieveCQLTestWithStartRecord() throws IOException, SAXException{
        ClientResponse response = resource().queryParam("operation", "searchRetrieve")
                .queryParam("query", "der")
                .queryParam("startRecord", "51")
                .get(ClientResponse.class);
      InputStream entity = response.getEntity(InputStream.class);
      Document doc = docBuilder.parse(entity);
      
      NodeList nodelist = doc.getElementsByTagName("sruResponse:recordPosition");
      assertEquals("51", nodelist.item(0).getTextContent());
    } 
	
	@Test
	public void searchRetrieveFCSQLTest() throws IOException, SAXException{
	    ClientResponse response = resource().queryParam("operation", "searchRetrieve")
                .queryParam("query", "[tt:lemma=\"fein\"]")
                .queryParam("queryType", "fcs")
                .queryParam("maximumRecords", "5")
                .get(ClientResponse.class);
        
//	    String entity = response.getEntity(String.class);
//	    System.out.println(entity);
//	    ByteArrayInputStream bis = new ByteArrayInputStream(entity.getBytes());
//	    checkSRUSearchRetrieveResponse(bis);
	    
        InputStream entity = response.getEntity(InputStream.class);
		checkSRUSearchRetrieveResponse(entity);
	} 
	
	private void checkSRUSearchRetrieveResponse(InputStream entity) throws SAXException, IOException {
		
		Document doc = docBuilder.parse(entity);
		
		NodeList nodelist = doc.getElementsByTagName("sruResponse:version");
		assertEquals("2.0", nodelist.item(0).getTextContent());
		nodelist = doc.getElementsByTagName("sruResponse:recordSchema");
		assertEquals("http://clarin.eu/fcs/resource", nodelist.item(0).getTextContent());
		
		NodeList resources = doc.getElementsByTagName("fcs:Resource");
		String attr  = resources.item(0).getAttributes().getNamedItem("pid").getNodeValue();
		
		nodelist = doc.getElementsByTagName("fcs:DataView");
		attr = nodelist.item(0).getAttributes().getNamedItem("type").getNodeValue();
		assertEquals("application/x-clarin-fcs-hits+xml", attr);
		
		Node node = nodelist.item(0).getFirstChild();
		assertEquals("hits:Result", node.getNodeName());
		NodeList children = node.getChildNodes();
		assertEquals("hits:Hit", children.item(1).getNodeName());
		
		attr = nodelist.item(1).getAttributes().getNamedItem("type").getNodeValue();
		assertEquals("application/x-clarin-fcs-adv+xml", attr);
		node = nodelist.item(1).getFirstChild();
		assertEquals("adv:Advanced", node.getNodeName());
		
		nodelist = node.getChildNodes();
		node = nodelist.item(0);
		assertEquals("adv:Segments", node.getNodeName());
		node = nodelist.item(1);
		assertEquals("adv:Layers", node.getNodeName());
		
		checkSegmentPosition(resources);
	}
	
	
	private void checkSegmentPosition (NodeList resources) {
        // dataviews
        NodeList childNodes = resources.item(1).getFirstChild().getChildNodes();
        // segments
        childNodes = childNodes.item(1).getFirstChild().getChildNodes().item(0)
                .getChildNodes();

        Node node = childNodes.item(0);
        // 1st Segment
        assertEquals("adv:Segment", node.getNodeName());
        assertEquals("0",
                node.getAttributes().getNamedItem("start").getNodeValue());

        // 4th segment
        node = childNodes.item(3);
        assertEquals("0",
                node.getAttributes().getNamedItem("start").getNodeValue());
    }

    @Test
    public void searchRetrieveWithResourceId() throws IOException, URISyntaxException, IllegalStateException, SAXException{
	    ClientResponse response = resource().queryParam("operation", "searchRetrieve")
                .queryParam("query", "fein")
                .queryParam("x-fcs-context", "GOE")
                .get(ClientResponse.class);
        
	    InputStream entity = response.getEntity(InputStream.class);
        checkSRUSearchRetrieveResponse(entity);
    } 
	
	@Test
	public void explainTest() throws URISyntaxException, ClientProtocolException, IOException, IllegalStateException, SAXException{
	    
	    ClientResponse response = resource().queryParam("operation", "explain")
                .get(ClientResponse.class);
        
        InputStream entity = response.getEntity(InputStream.class);
        
		assertEquals(200,response.getStatus());
		
		Document doc = docBuilder.parse(entity);
		NodeList nodelist = doc.getElementsByTagName("sruResponse:version");
		assertEquals("2.0", nodelist.item(0).getTextContent());
		
		nodelist = doc.getElementsByTagName("sruResponse:recordSchema");
		assertEquals("http://explain.z3950.org/dtd/2.0/", nodelist.item(0).getTextContent());
		
		nodelist = doc.getElementsByTagName("zr:serverInfo");
		NamedNodeMap attributes = nodelist.item(0).getAttributes();
		assertEquals("SRU", attributes.getNamedItem("protocol").getNodeValue());
		assertEquals("2.0", attributes.getNamedItem("version").getNodeValue()); 
		assertEquals("http", attributes.getNamedItem("transport").getNodeValue());
		
		NodeList children = nodelist.item(0).getChildNodes();
		assertEquals("zr:host", children.item(0).getNodeName());
		assertEquals("127.0.0.1", children.item(0).getTextContent());
		assertEquals("zr:port", children.item(1).getNodeName());
		assertEquals("8080", children.item(1).getTextContent());
		assertEquals("zr:database", children.item(2).getNodeName());
		assertEquals("korapsru", children.item(2).getTextContent());
		
		nodelist = doc.getElementsByTagName("zr:databaseInfo");
		children = nodelist.item(0).getChildNodes();
		assertEquals("zr:title", children.item(0).getNodeName());
		assertEquals("KorAP", children.item(0).getTextContent());
		
		nodelist = doc.getElementsByTagName("zr:indexInfo");
		children = nodelist.item(0).getChildNodes();
		assertEquals("zr:set", children.item(0).getNodeName());
		assertEquals("http://clarin.eu/fcs/resource", children.item(0).getAttributes().getNamedItem("identifier").getNodeValue());
		assertEquals("fcs", children.item(0).getAttributes().getNamedItem("name").getNodeValue());
		
		nodelist = doc.getElementsByTagName("zr:schemaInfo");
		children = nodelist.item(0).getChildNodes();
		assertEquals("zr:schema", children.item(0).getNodeName());
		assertEquals("http://clarin.eu/fcs/resource", children.item(0).getAttributes().getNamedItem("identifier").getNodeValue());
		assertEquals("fcs", children.item(0).getAttributes().getNamedItem("name").getNodeValue());
		
		nodelist = doc.getElementsByTagName("zr:configInfo");
		children = nodelist.item(0).getChildNodes();
		assertEquals("numberOfRecords", children.item(0).getAttributes().getNamedItem("type").getNodeValue());
		assertEquals("25", children.item(0).getTextContent());
		assertEquals("numberOfRecords", children.item(0).getAttributes().getNamedItem("type").getNodeValue());
		assertEquals("50", children.item(1).getTextContent());
		
		nodelist = doc.getElementsByTagName("ed:EndpointDescription");
		assertEquals(0, nodelist.getLength());
	}
	
	@Test
	public void explainEndpointDescriptionTest() throws URISyntaxException, ClientProtocolException, IOException, IllegalStateException, SAXException{
	    
	    String korapResources = IOUtils.toString(
                ClassLoader.getSystemResourceAsStream(
                        "korap-api-responses/resources.json"),
                StandardCharsets.UTF_8);
        
        mockClient.reset()
        .when(request().withMethod("GET").withPath("/resource"))
        .respond(response()
                .withHeader(new Header("Content-Type",
                        "application/json; charset=utf-8"))
                .withBody(korapResources).withStatusCode(200));
        
	    ClientResponse response = resource().queryParam("operation", "explain")
	            .queryParam("x-fcs-endpoint-description", "true")
                .get(ClientResponse.class);
        
        InputStream entity = response.getEntity(InputStream.class);
        
        assertEquals(200,response.getStatus());
        
        Document doc = docBuilder.parse(entity);
		NodeList nodelist = doc.getElementsByTagName("ed:EndpointDescription");
		assertEquals("2", nodelist.item(0).getAttributes().getNamedItem("version").getNodeValue());
		
		nodelist = doc.getElementsByTagName("ed:Capabilities");
		NodeList children = nodelist.item(0).getChildNodes();
		assertEquals("http://clarin.eu/fcs/capability/basic-search", children.item(0).getTextContent());
		assertEquals("http://clarin.eu/fcs/capability/advanced-search", children.item(1).getTextContent());
		
		nodelist = doc.getElementsByTagName("ed:SupportedDataViews");
		children = nodelist.item(0).getChildNodes();
		assertEquals("application/x-clarin-fcs-hits+xml", children.item(0).getTextContent());
		assertEquals("send-by-default", children.item(0).getAttributes().getNamedItem("delivery-policy").getNodeValue());
		assertEquals("application/x-clarin-fcs-adv+xml", children.item(1).getTextContent());
		assertEquals("send-by-default", children.item(1).getAttributes().getNamedItem("delivery-policy").getNodeValue());
		
		nodelist = doc.getElementsByTagName("ed:SupportedLayers");
		children = nodelist.item(0).getChildNodes();
		assertEquals(6, children.getLength());
		assertEquals("text", children.item(0).getTextContent());
		assertEquals("http://clarin.ids-mannheim.de/korapsru/layers/text", children.item(0).getAttributes().getNamedItem("result-id").getNodeValue());
		assertEquals("pos", children.item(1).getTextContent());
		assertEquals("corenlp", children.item(1).getAttributes().getNamedItem("qualifier").getNodeValue());
		
		
		nodelist = doc.getElementsByTagName("ed:Resource");
		assertEquals(3, nodelist.getLength());
		children = nodelist.item(0).getChildNodes();
		assertEquals(7, children.getLength());
		assertEquals("ed:Title", children.item(0).getNodeName());
		assertEquals("ed:Description", children.item(2).getNodeName());
		assertEquals("ed:LandingPageURI", children.item(3).getNodeName());
		assertEquals("ed:Languages", children.item(4).getNodeName());
		assertEquals("ed:AvailableDataViews", children.item(5).getNodeName());
		assertEquals("ed:AvailableLayers", children.item(6).getNodeName());
	}
	
}
