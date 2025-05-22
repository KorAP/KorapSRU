package de.ids_mannheim.korap.test;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.mockserver.model.Header;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.ClientResponse;

/**
 * The tests are based on the sample corpus from the Goethe corpus.
 * 
 * @author margaretha
 *
 */
public class KorapSRUTest extends BaseTest {

    public KorapSRUTest () throws ParserConfigurationException {
        docBuilder = factory.newDocumentBuilder();
    }

    private void createExpectationForSearchFein () throws IOException {
        String searchResult = IOUtils.toString(
                ClassLoader.getSystemResourceAsStream(
                        "korap-api-responses/search-fein.jsonld"),
                StandardCharsets.UTF_8);

        mockClient
                .when(request().withMethod("GET").withPath("/search")
                        .withQueryStringParameter("q", "fein")
                        .withQueryStringParameter("ql", "cql")
                        .withQueryStringParameter("v", "1.2")
                        .withQueryStringParameter("context", "sentence")
                        .withQueryStringParameter("count", "25")
                        .withQueryStringParameter("offset", "0"))
                .respond(response()
                        .withHeader(new Header("Content-Type",
                                "application/json; charset=utf-8"))
                        .withBody(searchResult).withStatusCode(200));
    }
    
    private void createExpectationForSearchFeinWithUnknownCq () throws IOException {
        String searchResult = IOUtils.toString(
                ClassLoader.getSystemResourceAsStream(
                        "korap-api-responses/search-fein-unknown-cq.jsonld"),
                StandardCharsets.UTF_8);

        mockClient
                .when(request().withMethod("GET").withPath("/search")
                        .withQueryStringParameter("q", "fein")
                        .withQueryStringParameter("ql", "cql")
                        .withQueryStringParameter("v", "1.2")
                        .withQueryStringParameter("context", "sentence")
                        .withQueryStringParameter("count", "25")
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("cq","corpusSigle=unknown"))
                .respond(response()
                        .withHeader(new Header("Content-Type",
                                "application/json; charset=utf-8"))
                        .withBody(searchResult).withStatusCode(200));
    }

    private void createExpectationForMatchInfoFein () throws IOException {
        String matchInfoResult = IOUtils.toString(
                ClassLoader.getSystemResourceAsStream(
                        "korap-api-responses/GOE-AGF-00000-p15205-15206.jsonld"),
                StandardCharsets.UTF_8);

        mockClient
                .when(request().withMethod("GET")
                        .withPath(
                                "/corpus/GOE/AGF/00000/p15205-15206/matchInfo")
                        .withQueryStringParameter("foundry", "*")
                        .withQueryStringParameter("spans", "false"))
                .respond(response()
                        .withHeader(new Header("Content-Type",
                                "application/json; charset=utf-8"))
                        .withBody(matchInfoResult).withStatusCode(200));

        matchInfoResult = IOUtils.toString(
                ClassLoader.getSystemResourceAsStream(
                        "korap-api-responses/GOE-AGF-00000-p15216-15217.jsonld"),
                StandardCharsets.UTF_8);

        mockClient
                .when(request().withMethod("GET")
                        .withPath(
                                "/corpus/GOE/AGF/00000/p15216-15217/matchInfo")
                        .withQueryStringParameter("foundry", "*")
                        .withQueryStringParameter("spans", "false"))
                .respond(response()
                        .withHeader(new Header("Content-Type",
                                "application/json; charset=utf-8"))
                        .withBody(matchInfoResult).withStatusCode(200));

        matchInfoResult = IOUtils.toString(
                ClassLoader.getSystemResourceAsStream(
                        "korap-api-responses/GOE-AGF-00000-p45002-45003.jsonld"),
                StandardCharsets.UTF_8);

        mockClient
                .when(request().withMethod("GET")
                        .withPath(
                                "/corpus/GOE/AGF/00000/p45002-45003/matchInfo")
                        .withQueryStringParameter("foundry", "*")
                        .withQueryStringParameter("spans", "false"))
                .respond(response()
                        .withHeader(new Header("Content-Type",
                                "application/json; charset=utf-8"))
                        .withBody(matchInfoResult).withStatusCode(200));

    }

    @Test
    public void searchRetrieveCQLTest ()
            throws IOException, SAXException, ParserConfigurationException {
        createExpectationForSearchFein();
        createExpectationForMatchInfoFein();

        ClientResponse response =
                resource().queryParam("operation", "searchRetrieve")
                        .queryParam("version", "1.2")
                        .queryParam("query", "fein").get(ClientResponse.class);

        InputStream entity = response.getEntity(InputStream.class);
        checkSearchRetrieveResponseSRUVersion1_2(entity);
    }

    @Test
    public void searchRetrieveWithResourceId ()
            throws IOException, URISyntaxException, IllegalStateException,
            SAXException, ParserConfigurationException {
    	createExpectationForRetrieveResource();
    	createExpectationForSearchFein();
        createExpectationForMatchInfoFein();

        ClientResponse response = resource()
                .queryParam("operation", "searchRetrieve")
                .queryParam("query", "fein").queryParam("version", "1.2")
                .queryParam("x-fcs-context", "http://hdl.handle.net/10932/00-03B6-558F-4E10-6201-1").get(ClientResponse.class);

        InputStream entity = response.getEntity(InputStream.class);
        checkSearchRetrieveResponseSRUVersion1_2(entity);
    }
    
    @Test
    public void searchRetrieveWithUnknownResourceId ()
            throws IOException, URISyntaxException, IllegalStateException,
            SAXException, ParserConfigurationException {
    	
    	createExpectationForRetrieveResource();
    	createExpectationForSearchFeinWithUnknownCq();
        createExpectationForMatchInfoFein();

        ClientResponse response = resource()
                .queryParam("operation", "searchRetrieve")
                .queryParam("query", "fein").queryParam("version", "1.2")
                .queryParam("x-fcs-context", "unknown").get(ClientResponse.class);

		InputStream entity = response.getEntity(InputStream.class);
		docBuilder = factory.newDocumentBuilder();
		Document doc = docBuilder.parse(entity);

		NodeList diagnosticUri = doc.getElementsByTagName("diag:uri");
		assertEquals("info:srw/diagnostic/1/1",
				diagnosticUri.item(0).getTextContent());

		NodeList diagnosticMessage = doc.getElementsByTagName("diag:message");
		assertEquals("Virtual corpus with pid: unknown is not found.",
				diagnosticMessage.item(0).getTextContent());
	}

    @Test
    public void searchRetrieveFCSQLTest ()
            throws IOException, SAXException, ParserConfigurationException {

        createExpectationForSearch("[tt:lemma=\"fein\"]", "fcsql", "2.0", "0",
                "search-lemma-fein.jsonld");
        createExpectationForMatchInfo("GOE-AGF-00000-p4276-4277.jsonld",
                "/corpus/GOE/AGF/00000/p4276-4277/matchInfo");

        ClientResponse response = resource()
                .queryParam("operation", "searchRetrieve")
                .queryParam("query", "[tt:lemma=\"fein\"]")
                .queryParam("queryType", "fcs")
                .queryParam("maximumRecords", "1").get(ClientResponse.class);

        InputStream entity = response.getEntity(InputStream.class);
        checkSearchRetrieveResponseSRUVersion2(entity);
    }

    @Test
    public void searchRetrieveCQLTestWithStartRecord ()
            throws IOException, SAXException {

        String searchResult = IOUtils.toString(
                ClassLoader.getSystemResourceAsStream(
                        "korap-api-responses/search-der.jsonld"),
                StandardCharsets.UTF_8);

        mockClient.reset()
                .when(request().withMethod("GET").withPath("/search")
                        .withQueryStringParameter("q", "der")
                        .withQueryStringParameter("ql", "cql")
                        .withQueryStringParameter("v", "1.2")
                        .withQueryStringParameter("context", "sentence")
                        .withQueryStringParameter("count", "1")
                        .withQueryStringParameter("offset", "50"))
                .respond(response()
                        .withHeader(new Header("Content-Type",
                                "application/json; charset=utf-8"))
                        .withBody(searchResult).withStatusCode(200));

        String matchInfoResult = IOUtils.toString(
                ClassLoader.getSystemResourceAsStream(
                        "korap-api-responses/GOE-AGA-01784-p1856-1857.jsonld"),
                StandardCharsets.UTF_8);

        mockClient
                .when(request().withMethod("GET")
                        .withPath("/corpus/GOE/AGA/01784/p1856-1857/matchInfo")
                        .withQueryStringParameter("foundry", "*")
                        .withQueryStringParameter("spans", "false"))
                .respond(response()
                        .withHeader(new Header("Content-Type",
                                "application/json; charset=utf-8"))
                        .withBody(matchInfoResult).withStatusCode(200));

        ClientResponse response = resource()
                .queryParam("operation", "searchRetrieve")
                .queryParam("query", "der").queryParam("startRecord", "51")
                .queryParam("version", "1.2").queryParam("maximumRecords", "1")
                .get(ClientResponse.class);
        InputStream entity = response.getEntity(InputStream.class);
        Document doc = docBuilder.parse(entity);

        NodeList nodelist =
                doc.getElementsByTagName("sru:recordPosition");
        assertEquals("51", nodelist.item(0).getTextContent());
    }

    @Test
    public void explainTest ()
            throws URISyntaxException, ClientProtocolException, IOException,
            IllegalStateException, SAXException {

        ClientResponse response = resource().queryParam("operation", "explain")
                .get(ClientResponse.class);

        InputStream entity = response.getEntity(InputStream.class);

        assertEquals(200, response.getStatus());

        Document doc = docBuilder.parse(entity);
        NodeList nodelist = doc.getElementsByTagName("sruResponse:version");
        assertEquals("2.0", nodelist.item(0).getTextContent());

        nodelist = doc.getElementsByTagName("sruResponse:recordSchema");
        assertEquals("http://explain.z3950.org/dtd/2.0/",
                nodelist.item(0).getTextContent());

        nodelist = doc.getElementsByTagName("zr:serverInfo");
        NamedNodeMap attributes = nodelist.item(0).getAttributes();
        assertEquals("SRU", attributes.getNamedItem("protocol").getNodeValue());
        assertEquals("2.0", attributes.getNamedItem("version").getNodeValue());
        assertEquals("http",
                attributes.getNamedItem("transport").getNodeValue());

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
        assertEquals("http://clarin.eu/fcs/resource", children.item(0)
                .getAttributes().getNamedItem("identifier").getNodeValue());
        assertEquals("fcs", children.item(0).getAttributes()
                .getNamedItem("name").getNodeValue());

        nodelist = doc.getElementsByTagName("zr:schemaInfo");
        children = nodelist.item(0).getChildNodes();
        assertEquals("zr:schema", children.item(0).getNodeName());
        assertEquals("http://clarin.eu/fcs/resource", children.item(0)
                .getAttributes().getNamedItem("identifier").getNodeValue());
        assertEquals("fcs", children.item(0).getAttributes()
                .getNamedItem("name").getNodeValue());

        nodelist = doc.getElementsByTagName("zr:configInfo");
        children = nodelist.item(0).getChildNodes();
        assertEquals("numberOfRecords", children.item(0).getAttributes()
                .getNamedItem("type").getNodeValue());
        assertEquals("25", children.item(0).getTextContent());
        assertEquals("numberOfRecords", children.item(0).getAttributes()
                .getNamedItem("type").getNodeValue());
        assertEquals("50", children.item(1).getTextContent());

        nodelist = doc.getElementsByTagName("ed:EndpointDescription");
        assertEquals(0, nodelist.getLength());
    }

    @Test
    public void explainEndpointDescriptionTest ()
            throws URISyntaxException, ClientProtocolException, IOException,
            IllegalStateException, SAXException {

        createExpectationForRetrieveResource();

        ClientResponse response = resource().queryParam("operation", "explain")
                .queryParam("x-fcs-endpoint-description", "true")
                .get(ClientResponse.class);

        InputStream entity = response.getEntity(InputStream.class);

        assertEquals(200, response.getStatus());

        Document doc = docBuilder.parse(entity);
        NodeList nodelist = doc.getElementsByTagName("ed:EndpointDescription");
        assertEquals("2", nodelist.item(0).getAttributes()
                .getNamedItem("version").getNodeValue());

        nodelist = doc.getElementsByTagName("ed:Capabilities");
        NodeList children = nodelist.item(0).getChildNodes();
        assertEquals("http://clarin.eu/fcs/capability/basic-search",
                children.item(0).getTextContent());
        assertEquals("http://clarin.eu/fcs/capability/advanced-search",
                children.item(1).getTextContent());

        nodelist = doc.getElementsByTagName("ed:SupportedDataViews");
        children = nodelist.item(0).getChildNodes();
        assertEquals("application/x-clarin-fcs-hits+xml",
                children.item(0).getTextContent());
        assertEquals("send-by-default", children.item(0).getAttributes()
                .getNamedItem("delivery-policy").getNodeValue());
        assertEquals("application/x-clarin-fcs-adv+xml",
                children.item(1).getTextContent());
        assertEquals("send-by-default", children.item(1).getAttributes()
                .getNamedItem("delivery-policy").getNodeValue());

        nodelist = doc.getElementsByTagName("ed:SupportedLayers");
        children = nodelist.item(0).getChildNodes();
        assertEquals(6, children.getLength());
        assertEquals("text", children.item(0).getTextContent());
        assertEquals("http://clarin.ids-mannheim.de/korapsru/layers/text",
                children.item(0).getAttributes().getNamedItem("result-id")
                        .getNodeValue());
        assertEquals("pos", children.item(1).getTextContent());
        assertEquals("corenlp", children.item(1).getAttributes()
                .getNamedItem("qualifier").getNodeValue());

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
