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
    protected DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

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
    
    protected void createExpectationForSearchLemmaFein () throws IOException {
        String searchResult = IOUtils.toString(
                ClassLoader.getSystemResourceAsStream(
                        "korap-api-responses/search-lemma-fein.jsonld"),
                StandardCharsets.UTF_8);

        mockClient.reset()
                .when(request().withMethod("GET").withPath("/search")
                        .withQueryStringParameter("q", "[tt:lemma=\"fein\"]")
                        .withQueryStringParameter("ql", "fcsql")
                        .withQueryStringParameter("v", "2.0")
                        .withQueryStringParameter("context", "sentence")
                        .withQueryStringParameter("count", "1")
                        .withQueryStringParameter("offset", "0"))
                .respond(response()
                        .withHeader(new Header("Content-Type",
                                "application/json; charset=utf-8"))
                        .withBody(searchResult).withStatusCode(200));
    }
    
    protected void createExpectationForMatchInfoLemmaFein() throws IOException{
        String matchInfoResult = IOUtils.toString(
                ClassLoader.getSystemResourceAsStream(
                        "korap-api-responses/GOE-AGF-00000-p4276-4277.jsonld"),
                StandardCharsets.UTF_8);

        mockClient
                .when(request().withMethod("GET")
                        .withPath("/corpus/GOE/AGF/00000/p4276-4277/matchInfo")
                        .withQueryStringParameter("foundry", "*")
                        .withQueryStringParameter("spans", "false"))
                .respond(response()
                        .withHeader(new Header("Content-Type",
                                "application/json; charset=utf-8"))
                        .withBody(matchInfoResult).withStatusCode(200));
    }

    protected void checkSRUSearchRetrieveResponse (InputStream entity)
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
