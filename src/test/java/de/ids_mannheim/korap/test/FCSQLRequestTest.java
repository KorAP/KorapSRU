package de.ids_mannheim.korap.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.ClientResponse;

/**
 * @author margaretha
 *
 */
public class FCSQLRequestTest extends BaseTest {

    private static DocumentBuilder documentBuilder;

    @BeforeClass
    public static void setDocumentBuilder ()
            throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory =
                DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }

    @Test
    public void testLemmaRegex () throws URISyntaxException, IOException,
            SAXException, ParserConfigurationException {

        createExpectationForSearch("[tt:lemma=\".*bar\"]",
                "search-lemma-bar.jsonld");
        createExpectationForMatchInfoLemmaBar();

        ClientResponse response = resource()
                .queryParam("operation", "searchRetrieve")
                .queryParam("query", "[tt:lemma=\".*bar\"]")
                .queryParam("queryType", "fcs")
                .queryParam("maximumRecords", "1").get(ClientResponse.class);

        InputStream entity = response.getEntity(InputStream.class);
        Document doc = checkSRUSearchRetrieveResponse(entity);

        NodeList nodeList =
                doc.getElementsByTagName("sruResponse:numberOfRecords");

        assertEquals("132", nodeList.item(0).getTextContent());

    }

    @Test
    public void testUnsupportedLayer () throws URISyntaxException, IOException,
            SAXException, ParserConfigurationException {

        createExpectationForSearch("[unknown=\"fein\"]", "unknownLayer.jsonld");

        ClientResponse response = resource()
                .queryParam("operation", "searchRetrieve")
                .queryParam("query", "[unknown=\"fein\"]")
                .queryParam("queryType", "fcs")
                .queryParam("maximumRecords", "1").get(ClientResponse.class);

        InputStream is = response.getEntity(InputStream.class);

        Document document = documentBuilder.parse(is);
        NodeList nodeList = document.getElementsByTagName("diag:message");
        assertEquals("Layer unknown is unsupported.",
                nodeList.item(0).getTextContent());
        response.close();
    }

    @Test
    public void testUnsupportedQualifer () throws URISyntaxException,
            IOException, SAXException, ParserConfigurationException {
        createExpectationForSearch("[unknown:lemma=\"fein\"]",
                "unknownQualifier.jsonld");

        ClientResponse response = resource()
                .queryParam("operation", "searchRetrieve")
                .queryParam("query", "[unknown:lemma=\"fein\"]")
                .queryParam("queryType", "fcs")
                .queryParam("maximumRecords", "1").get(ClientResponse.class);

        InputStream is = response.getEntity(InputStream.class);

        Document document = documentBuilder.parse(is);
        NodeList nodeList = document.getElementsByTagName("diag:message");
        assertEquals("Qualifier unknown is unsupported.",
                nodeList.item(0).getTextContent());
        response.close();
    }
}
