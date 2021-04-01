package de.ids_mannheim.korap.test;

import static org.junit.Assert.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BaseTest extends KorapJerseyTest {

    protected DocumentBuilder docBuilder;
    protected DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();

    protected ClientAndServer mockServer;
    protected MockServerClient mockClient;

    @Before
    public void startMockServer () {
        mockServer = startClientAndServer(1080);
        mockClient = new MockServerClient("localhost", mockServer.getPort());
    }

    @After
    public void stopMockServer () {
        mockServer.stop();
    }

    protected void createRetrieveResource () throws IOException {
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
    }

    protected void createExpectationForSearch (String query,
            String queryLanguage, String version, String offset,
            String jsonFilename) throws IOException {
        String searchResult = IOUtils.toString(
                ClassLoader.getSystemResourceAsStream(
                        "korap-api-responses/" + jsonFilename),
                StandardCharsets.UTF_8);

        mockClient.reset()
                .when(request().withMethod("GET").withPath("/search")
                        .withQueryStringParameter("q", query)
                        .withQueryStringParameter("ql", queryLanguage)
                        .withQueryStringParameter("v", version)
                        .withQueryStringParameter("context", "sentence")
                        .withQueryStringParameter("count", "1")
                        .withQueryStringParameter("offset", offset))
                .respond(response()
                        .withHeader(new Header("Content-Type",
                                "application/json; charset=utf-8"))
                        .withBody(searchResult).withStatusCode(200));
    }

    protected void createExpectationForMatchInfo (String jsonFilename,
            String uriPath) throws IOException {
        String matchInfoResult = IOUtils.toString(
                ClassLoader.getSystemResourceAsStream(
                        "korap-api-responses/" + jsonFilename),
                StandardCharsets.UTF_8);

        mockClient
                .when(request().withMethod("GET").withPath(uriPath)
                        .withQueryStringParameter("foundry", "*")
                        .withQueryStringParameter("spans", "false"))
                .respond(response()
                        .withHeader(new Header("Content-Type",
                                "application/json; charset=utf-8"))
                        .withBody(matchInfoResult).withStatusCode(200));
    }

    protected Document checkSearchRetrieveResponseSRUVersion1_2 (InputStream entity)
            throws SAXException, IOException, ParserConfigurationException {

        docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(entity);

        NodeList nodelist = doc.getElementsByTagName("sru:version");
        assertEquals("1.2", nodelist.item(0).getTextContent());
        nodelist = doc.getElementsByTagName("sru:recordSchema");
        assertEquals("http://clarin.eu/fcs/resource",
                nodelist.item(0).getTextContent());

        NodeList resources = doc.getElementsByTagName("fcs:Resource");
        String attr = resources.item(0).getAttributes().getNamedItem("pid")
                .getNodeValue();

        nodelist = doc.getElementsByTagName("fcs:DataView");
        attr = nodelist.item(0).getAttributes().getNamedItem("type")
                .getNodeValue();
        assertEquals("application/x-clarin-fcs-hits+xml", attr);

        Node node = nodelist.item(0).getFirstChild();
        assertEquals("hits:Result", node.getNodeName());
        NodeList children = node.getChildNodes();
        if (children.getLength() > 1) {
            assertEquals("hits:Hit", children.item(1).getNodeName());
        }

        attr = nodelist.item(1).getAttributes().getNamedItem("type")
                .getNodeValue();
        assertEquals("application/x-clarin-fcs-adv+xml", attr);
        node = nodelist.item(1).getFirstChild();
        assertEquals("adv:Advanced", node.getNodeName());

        nodelist = node.getChildNodes();
        node = nodelist.item(0);
        assertEquals("adv:Segments", node.getNodeName());
        node = nodelist.item(1);
        assertEquals("adv:Layers", node.getNodeName());

        checkSegmentPosition(resources);
        return doc;
    }
    
    protected Document checkSearchRetrieveResponseSRUVersion2 (InputStream entity)
            throws SAXException, IOException, ParserConfigurationException {

        docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(entity);

        NodeList nodelist = doc.getElementsByTagName("sruResponse:version");
        assertEquals("2.0", nodelist.item(0).getTextContent());
        nodelist = doc.getElementsByTagName("sruResponse:recordSchema");
        assertEquals("http://clarin.eu/fcs/resource",
                nodelist.item(0).getTextContent());

        NodeList resources = doc.getElementsByTagName("fcs:Resource");
        String attr = resources.item(0).getAttributes().getNamedItem("pid")
                .getNodeValue();

        nodelist = doc.getElementsByTagName("fcs:DataView");
        attr = nodelist.item(0).getAttributes().getNamedItem("type")
                .getNodeValue();
        assertEquals("application/x-clarin-fcs-hits+xml", attr);

        Node node = nodelist.item(0).getFirstChild();
        assertEquals("hits:Result", node.getNodeName());
        NodeList children = node.getChildNodes();
        if (children.getLength() > 1) {
            assertEquals("hits:Hit", children.item(1).getNodeName());
        }

        attr = nodelist.item(1).getAttributes().getNamedItem("type")
                .getNodeValue();
        assertEquals("application/x-clarin-fcs-adv+xml", attr);
        node = nodelist.item(1).getFirstChild();
        assertEquals("adv:Advanced", node.getNodeName());

        nodelist = node.getChildNodes();
        node = nodelist.item(0);
        assertEquals("adv:Segments", node.getNodeName());
        node = nodelist.item(1);
        assertEquals("adv:Layers", node.getNodeName());

        checkSegmentPosition(resources);
        return doc;
    }

    protected void checkSegmentPosition (NodeList resources) {
        // dataviews
        NodeList childNodes = resources.item(0).getFirstChild().getChildNodes();
        // segments
        childNodes = childNodes.item(1).getFirstChild().getChildNodes().item(0)
                .getChildNodes();

        Node node = childNodes.item(0);
        // 1st Segment
        assertEquals("adv:Segment", node.getNodeName());
        assertEquals("0",
                node.getAttributes().getNamedItem("start").getNodeValue());

        if (childNodes.getLength() > 3) {
            // 4th segment
            node = childNodes.item(3);
            assertEquals("0",
                    node.getAttributes().getNamedItem("start").getNodeValue());
        }
    }
}
