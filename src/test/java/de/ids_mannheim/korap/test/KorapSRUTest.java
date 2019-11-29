package de.ids_mannheim.korap.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
	private int port = 8080;
	private String host = "localhost";
	private DocumentBuilder docBuilder;
	
	public KorapSRUTest() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		docBuilder = factory.newDocumentBuilder();
	}
	
	@Test
	public void searchRetrieveCQLTest() throws IOException, URISyntaxException, IllegalStateException, SAXException{
		HttpClient httpclient = HttpClients.createDefault();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(host).setPort(port).setPath("/KorapSRU")
				.setParameter("operation", "searchRetrieve")
				.setParameter("query", "fein");
		
		URI uri = builder.build();
		HttpGet request = new HttpGet(uri);
		HttpResponse response = httpclient.execute(request);
		checkSRUSearchRetrieveResponse(response);
	} 
	
	@Test
	public void searchRetrieveFCSQLTest() throws IOException, URISyntaxException, IllegalStateException, SAXException{
		HttpClient httpclient = HttpClients.createDefault();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(host).setPort(port).setPath("/KorapSRU")
				.setParameter("operation", "searchRetrieve")
				.setParameter("query", "[tt:lemma=\"fein\"]")
				.setParameter("queryType", "fcs");
		
		URI uri = builder.build();
		HttpGet request = new HttpGet(uri);
		HttpResponse response = httpclient.execute(request);
		checkSRUSearchRetrieveResponse(response);
	} 
	
	private void checkSRUSearchRetrieveResponse(HttpResponse response) throws IllegalStateException, SAXException, IOException {
		assertEquals(200,response.getStatusLine().getStatusCode());
		
		InputStream is = response.getEntity().getContent();
		Document doc = docBuilder.parse(is);
		NodeList nodelist = doc.getElementsByTagName("sruResponse:version");
		assertEquals("2.0", nodelist.item(0).getTextContent());
		nodelist = doc.getElementsByTagName("sruResponse:recordSchema");
		assertEquals("http://clarin.eu/fcs/resource", nodelist.item(0).getTextContent());
		
		nodelist = doc.getElementsByTagName("fcs:Resource");
		String attr  = nodelist.item(0).getAttributes().getNamedItem("pid").getNodeValue();
//		assertEquals("match-GOE/AGF/00000-p15205-15206", attr);
		
		nodelist = doc.getElementsByTagName("fcs:DataView");
		attr = nodelist.item(0).getAttributes().getNamedItem("type").getNodeValue();
		assertEquals("application/x-clarin-fcs-hits+xml", attr);
		Node node = nodelist.item(0).getFirstChild();
		assertEquals("hits:Result", node.getNodeName());
		
		NodeList children = node.getChildNodes();
		assertEquals("hits:Hit", children.item(1).getNodeName());
//		assertEquals("feineren", children.item(1).getTextContent());
		
		attr = nodelist.item(1).getAttributes().getNamedItem("type").getNodeValue();
		assertEquals("application/x-clarin-fcs-adv+xml", attr);
		node = nodelist.item(1).getFirstChild();
		assertEquals("adv:Advanced", node.getNodeName());
		
		nodelist = node.getChildNodes();
		node = nodelist.item(0);
		assertEquals("adv:Segments", node.getNodeName());
//		assertEquals(52, node.getChildNodes().getLength());
		node = nodelist.item(1);
		assertEquals("adv:Layers", node.getNodeName());
//		assertEquals(6, node.getChildNodes().getLength());
		
//		node = node.getFirstChild();
//		attr = node.getAttributes().getNamedItem("id").getNodeValue();
//		assertEquals("http://clarin.ids-mannheim.de/korapsru/layers/pos/marmot", attr);
//		assertEquals(50, node.getChildNodes().getLength());
	}
	
	@Test
	public void explainTest() throws URISyntaxException, ClientProtocolException, IOException, IllegalStateException, SAXException{
		HttpClient httpclient = HttpClients.createDefault();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(host).setPort(port).setPath("/KorapSRU")
				.setParameter("operation", "explain");
		
		URI uri = builder.build();
		HttpGet request = new HttpGet(uri);
		HttpResponse response = httpclient.execute(request);
		
		assertEquals(200,response.getStatusLine().getStatusCode());
		
		Document doc = docBuilder.parse(response.getEntity().getContent());
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
		HttpClient httpclient = HttpClients.createDefault();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(host).setPort(port).setPath("/KorapSRU")
				.setParameter("operation", "explain")
				.setParameter("x-fcs-endpoint-description", "true");
		
		URI uri = builder.build();
		HttpGet request = new HttpGet(uri);
		HttpResponse response = httpclient.execute(request);
		
		assertEquals(200,response.getStatusLine().getStatusCode());
		
		Document doc = docBuilder.parse(response.getEntity().getContent());
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
		for (int i=0; i<nodelist.getLength();i++){
			if (nodelist.item(i).getAttributes().getNamedItem("pid").equals("GOE")){
				children = nodelist.item(i).getChildNodes();
				assertEquals("ed:Title", children.item(0).getNodeName());
				assertEquals("Goethe", children.item(0).getTextContent());
			}
		}
	}
}
