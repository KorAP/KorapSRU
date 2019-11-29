package de.ids_mannheim.korap.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The tests in this class require a running KustvaktServer.
 * The results are based on the sample corpus from the Goethe corpus.
 * 
 * Specify the Kustvakt service URI at
 * /KorapSRU/src/main/webapp/WEB-INF/web.xml
 * 
 * @author margaretha
 *
 */
public class FCSQLRequestTest extends KorapJerseyTest {

    private String korapSruUri = "http://localhost:8080/KorapSRU";
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
    public void testFCSQuery () throws URISyntaxException, IOException,
            SAXException, ParserConfigurationException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("operation", "startRetrieve"));
        params.add(new BasicNameValuePair("query", "[tt:lemma=\".*bar\"]"));
        params.add(new BasicNameValuePair("queryType", "fcs"));

        URIBuilder builder = new URIBuilder(korapSruUri);
        builder.addParameters(params);

        URI uri = builder.build();
        assertEquals(korapSruUri + "?operation=startRetrieve&query="
                + "%5Btt%3Alemma%3D%22.*bar%22%5D&queryType=fcs",
                uri.toString());

        HttpGet request = new HttpGet(uri);
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        response = client.execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());

        InputStream is = response.getEntity().getContent();
        Document document = documentBuilder.parse(is);
        NodeList nodeList =
                document.getElementsByTagName("sruResponse:numberOfRecords");

        assertEquals("134", nodeList.item(0).getTextContent());
        response.close();
    }
    
    @Test
    public void testLemmaRegex () throws URISyntaxException, IOException,
            SAXException, ParserConfigurationException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("operation", "startRetrieve"));
        params.add(new BasicNameValuePair("query", "[lemma=\".*bar\"]"));
        params.add(new BasicNameValuePair("queryType", "fcs"));

        URIBuilder builder = new URIBuilder(korapSruUri);
        builder.addParameters(params);

        URI uri = builder.build();
        assertEquals(korapSruUri + "?operation=startRetrieve&query=%5Blemma"
                + "%3D%22.*bar%22%5D&queryType=fcs",
                uri.toString());

        HttpGet request = new HttpGet(uri);
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        response = client.execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());

        InputStream is = response.getEntity().getContent();
        Document document = documentBuilder.parse(is);
        NodeList nodeList =
                document.getElementsByTagName("sruResponse:numberOfRecords");

        assertEquals("134", nodeList.item(0).getTextContent());
        response.close();
    }

}
