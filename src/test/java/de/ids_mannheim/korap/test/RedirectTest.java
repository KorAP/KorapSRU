package de.ids_mannheim.korap.test;

import static org.mockserver.model.HttpOverrideForwardedRequest.forwardOverriddenRequest;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.mockserver.model.Header;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.ClientResponse;

/**
 * 
 * @author margaretha
 *
 */
public class RedirectTest extends BaseTest {

    @Test
    public void testRedirect () throws ClientProtocolException, IOException,
            SAXException, ParserConfigurationException {

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
                .forward(forwardOverriddenRequest(
                        request().withPath("/redirect")));

        mockClient
                .when(request().withMethod("GET").withPath("/redirect")
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
}
