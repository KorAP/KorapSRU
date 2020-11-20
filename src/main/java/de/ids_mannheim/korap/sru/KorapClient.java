package de.ids_mannheim.korap.sru;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.util.RedirectStrategy;

/**
 * Client to KorAP public services supporting calls to the resource,
 * search and matchInfo APIs.
 * 
 * @author margaretha
 * 
 */
public class KorapClient {

    private String serviceUri;
    private static final String DEFAULT_CONTEXT_TYPE = "sentence";
    private static final String DEFAULT_FOUNDRY = "*";

    private int defaultNumOfRecords = 25;
    private int defaultMaxRecords = 50;

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static Logger logger =
            (Logger) LoggerFactory.getLogger(KorapClient.class);

    /**
     * Constructs a KorapClient with the given number of records per
     * page and the maximum number of records.
     * 
     * @param serviceUri
     *            KorAP service URI
     * @param numOfRecords
     *            the number of records per page
     * @param maxRecords
     *            the number of maximum records/matches to retrieve
     * @throws FileNotFoundException
     */
    public KorapClient (String serviceUri, int numOfRecords, int maxRecords) {
        this.defaultNumOfRecords = numOfRecords;
        this.defaultMaxRecords = maxRecords;
        this.serviceUri = serviceUri;
    }

    /**
     * Gets information about available resources to search through
     * the KorAP public services.
     * 
     * @return a JSON node containing information about the resources
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    public KorapResource[] retrieveResources ()
            throws URISyntaxException, IOException {

        URIBuilder builder = new URIBuilder(serviceUri + "/resource");
        URI uri = builder.build();
        logger.info("Resource URI: " + uri.toString());
        HttpGet httpRequest = new HttpGet(uri);

        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        KorapResource[] resources = null;

        try {
            response = client.execute(httpRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                logger.warn("Error response code: " + statusCode);
                logger.warn("Error message: "
                        + response.getStatusLine().getReasonPhrase());
                throw new HttpResponseException(statusCode,
                        response.getStatusLine().getReasonPhrase());
            }

            InputStream jsonStream = response.getEntity().getContent();
            try {
                resources = objectMapper.readValue(jsonStream, KorapResource[].class);
            }
            catch (JsonParseException | JsonMappingException e) {
                throw e;
            }
            finally {
                jsonStream.close();
            }
        }
        finally {
            response.close();
        }

        return resources;
    }

    /**
     * Sends the given query to KorAP search API and creates a
     * KorapResult from the response.
     * 
     * @param query
     *            a query string
     * @param queryLanguage
     *            the query language
     * @param version
     *            the query language version
     * @param startRecord
     *            the starting record/match number to retrieve
     * @param maximumRecords
     *            the number of maximum records/matches to retrieve
     * @param corpora
     *            the corpora to search on
     * @return a KorapResult
     * 
     * @throws HttpResponseException
     * @throws IOException
     */
    public KorapResult query (String query, QueryLanguage queryLanguage,
            String version, int startRecord, int maximumRecords,
            String[] corpora) throws HttpResponseException, IOException {

        if (query == null) {
            throw new NullPointerException("Query is null.");
        }
        if (query.isEmpty()) {
            throw new IllegalArgumentException("Query is empty.");
        }
        if (startRecord < 1) {
            throw new IllegalArgumentException("Start record begins from 1.");
        }
        if (maximumRecords < 1) {
            throw new IllegalArgumentException("Maximum records is too low.");
        }

        HttpUriRequest httpRequest = null;
        try {
            httpRequest = createSearchRequest(query, queryLanguage, version,
                    startRecord - 1, maximumRecords, corpora);
        }
        catch (URISyntaxException e) {
            throw new IOException("Failed creating http request.");
        }

        CloseableHttpResponse response = null;
        KorapResult result = null;
        try {
            response = sendRequest(httpRequest);
//            logger.info(response.toString());
            
            BufferedInputStream jsonStream =
                    new BufferedInputStream(response.getEntity().getContent());
            try {
                result = objectMapper.readValue(jsonStream, KorapResult.class);
            }
//            catch (IOException e) {
//                throw new IOException("Failed processing response.");
//            }
            finally {
                jsonStream.close();
            }
        }
//        catch (IOException e) {
//            throw new IOException("Failed executing HTTP request.", e);
//        }
        finally {
            if (response != null) {
                response.close();
            }
        }

        return result;
    }

    private CloseableHttpResponse sendRequest (HttpUriRequest httpRequest)
            throws ClientProtocolException, IOException {
        CloseableHttpClient client = HttpClientBuilder.create()
                .setRedirectStrategy(new RedirectStrategy()).build();
        CloseableHttpResponse response = client.execute(httpRequest);

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            logger.warn("Error response code: " + statusCode);
            parseError(response);
        }
        return response;
    }

    /**
     * Parses the error message from Kustvakt (probably an old
     * format).
     * 
     * @param response
     *            a response from Kustvakt
     * @throws IOException
     */
    private static void parseError (CloseableHttpResponse response)
            throws IOException {

        logger.warn(
                "Error message: " + response.getStatusLine().getReasonPhrase());

        InputStream is = response.getEntity().getContent();
        JsonNode node = objectMapper.readTree(is);
        node = node.at("/errors");
        String errorCode="", errorMessage="";
        for (int i=0; i<node.size(); i++){
            errorCode = node.get(i).get(0).asText();
            errorMessage= node.get(i).get(1).asText();
            if (errorCode.contains("SRU diagnostic")){
                errorCode = errorCode.replace("SRU diagnostic ", "");
            }
            // see de.ids_mannheim.korap.query.serialize.util.StatusCodes
            else if (errorCode.equals("301")){
                errorCode="7"; // Mandatory parameter not supplied
            }
            else if (errorCode.equals("302") || errorCode.equals("303")
                    || errorCode.equals("304") || errorCode.equals("305")) {
                errorCode = "10"; // Query syntax error
            }
            else if (errorCode.equals("399")){
                errorCode="47"; // Cannot process query; reason unknown 
            }
            else if (errorCode.equals("306") || errorCode.equals("307") ){ 
                errorCode = "6"; // Unsupported parameter value
            }
            else{
                errorCode = "1"; // General system error
            }
        }
        System.out.println(errorCode +" "+errorMessage);
        throw new HttpResponseException(Integer.parseInt(errorCode),
                errorMessage);
    }

    /**
     * Builds a search retrieve GET request for the given parameters.
     * 
     * @param query
     *            a query string
     * @param queryLanguage
     *            the query language
     * @param version
     *            the query language version
     * @param startRecord
     *            the starting number of records/matches to return
     * @param maximumRecords
     *            the number of maximum records to return
     * @param corpora 
     * @return a HttpGet request
     * @throws URISyntaxException
     */
    private HttpGet createSearchRequest (String query,
            QueryLanguage queryLanguage, String version, int startRecord,
            int maximumRecords, String[] corpora) throws URISyntaxException {

        if (maximumRecords <= 0) {
            maximumRecords = defaultNumOfRecords;
        }
        else if (maximumRecords > defaultMaxRecords) {
            logger.info("limit truncated from {} to {}", maximumRecords,
                    defaultMaxRecords);
            maximumRecords = defaultMaxRecords;
        }
        
        String corpusQuery = "";
        if (corpora != null && corpora.length > 0) {
            for (int i = 0; i < corpora.length; i++) {
                corpusQuery += "corpusSigle=" + corpora[i];
                if (i != corpora.length - 1) {
                    corpusQuery += "|";
                }
            }
        }

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("q", query));
        params.add(new BasicNameValuePair("ql", queryLanguage.toString()));
        if (!corpusQuery.isEmpty()){
            params.add(new BasicNameValuePair("cq", corpusQuery));
        }
        params.add(new BasicNameValuePair("v", version));
        params.add(new BasicNameValuePair("context", DEFAULT_CONTEXT_TYPE));
        params.add(new BasicNameValuePair("count",
                String.valueOf(maximumRecords)));
        params.add(
                new BasicNameValuePair("offset", String.valueOf(startRecord)));

        URIBuilder builder = new URIBuilder(serviceUri + "/search");
        builder.addParameters(params);

        URI uri = builder.build();
        logger.info("Query URI: " + uri.toString());
        HttpGet request = new HttpGet(uri);
        return request;
    }

    /**
     * Sends a request to the MatchInfo API to get the annotations of
     * a particular match identified with corpus/resource id, document
     * id, and position id in the document in one or multiple
     * foundries.
     * 
     * @param resourceId
     *            the id of the corpus
     * @param documentId
     *            the id of the document
     * @param matchId
     *            the id of the match
     * @param foundry
     *            the annotation layer
     * @return the annotation snippet
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    public String retrieveAnnotations (String resourceId, String documentId,
            String textId, String matchId, String foundry)
            throws IOException, URISyntaxException {

        if (resourceId == null) {
            throw new NullPointerException("Corpus id of the match is null.");
        }
        else if (resourceId.isEmpty()) {
            throw new IllegalArgumentException(
                    "Corpus id of the match is empty.");
        }

        if (documentId == null) {
            throw new NullPointerException("Document id of the match is null.");
        }
        else if (documentId.isEmpty()) {
            throw new IllegalArgumentException(
                    "Document id of the match is empty.");
        }

        if (matchId == null) {
            throw new NullPointerException("Position id of the match is null.");
        }
        else if (matchId.isEmpty()) {
            throw new IllegalArgumentException(
                    "Position id of the match is empty.");
        }

        if (foundry == null | foundry.isEmpty()) {
            foundry = DEFAULT_FOUNDRY;
        }

        HttpUriRequest httpRequest = createMatchInfoRequest(resourceId,
                documentId, textId, matchId, foundry);

        String annotationSnippet = null;

        CloseableHttpResponse response = null;
        try {
            response = sendRequest(httpRequest);

            BufferedInputStream jsonStream =
                    new BufferedInputStream(response.getEntity().getContent());
            try {
                JsonNode root = objectMapper.readTree(jsonStream);
                annotationSnippet = "<snippet>" + root.at("/snippet").asText()
                        + "</snippet>";
            }
            catch (IOException e) {
                logger.error(e.getMessage());
                throw new IOException(
                        "Failed processing response from KorAP match info API.");
            }
            finally {
                jsonStream.close();
            }
        }
        finally {
            response.close();
        }
        return annotationSnippet;
    }

    /**
     * Builds a request URL to send to the KorAP MatchInfo service.
     * 
     * @param resourceId
     *            the id of the corpus
     * @param documentId
     *            the id of the document
     * @param matchId
     *            the id of the match
     * @param foundry
     *            the annotation layer
     * @return a HttpGet request
     * @throws URISyntaxException
     */
    private HttpGet createMatchInfoRequest (String resourceId,
            String documentId, String textId, String matchId, String foundry)
            throws URISyntaxException {

        StringBuilder sb = new StringBuilder();
        sb.append(serviceUri);
        sb.append("/corpus/");
        sb.append(resourceId);
        sb.append("/");
        sb.append(documentId);
        sb.append("/");
        sb.append(textId);
        sb.append("/");
        sb.append(matchId);
        sb.append("/matchInfo?foundry=");
        sb.append(foundry);
        sb.append("&spans=false");

        URIBuilder builder = new URIBuilder(sb.toString());
        URI uri = builder.build();
        logger.info("MatchInfo URI: " + uri.toString());
        HttpGet request = new HttpGet(uri);
        return request;
    }
}
