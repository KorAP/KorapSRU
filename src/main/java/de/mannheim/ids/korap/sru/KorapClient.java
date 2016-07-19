package de.mannheim.ids.korap.sru;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KorapClient {
	
	private static final String SERVICE_URI = "http://10.0.10.13:7070/api/v0.1/";
	private String CONTEXT_TYPE = "sentence";
	
	private int defaultNumOfRecords = 10;
	private int defaultMaxRecords = 10;
	
	private ObjectMapper objectMapper;
	private Logger logger = (Logger) LoggerFactory.getLogger(KorapClient.class);
	
	public KorapClient(int numOfRecords, int maxRecords) {
		this.objectMapper = new ObjectMapper();
		this.defaultNumOfRecords = numOfRecords;
		this.defaultMaxRecords = maxRecords;
	}
	
	public JsonNode retrieveResources() throws URISyntaxException, IOException {
		
		URIBuilder builder = new URIBuilder(SERVICE_URI+"VirtualCollection");
		//builder.addParameter("type", "VirtualCollection");
		URI uri = builder.build();
		logger.info("Resource URI: "+ uri.toString());
		HttpGet httpRequest = new HttpGet(uri);
		
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = null;		
		JsonNode resources = null;
		
		try {
			response = client.execute(httpRequest);
			
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK){
				logger.warn("Error response code: "+statusCode);
				logger.warn("Error message: "+response.getStatusLine().getReasonPhrase());
				throw new HttpResponseException(statusCode,  
						response.getStatusLine().getReasonPhrase()
				);
			}
			
			BufferedInputStream jsonStream = new BufferedInputStream( 
					response.getEntity().getContent() );
			try {				
				resources = objectMapper.readValue(jsonStream, JsonNode.class);
			} catch (JsonParseException | JsonMappingException e) {
				throw e;
			}
			finally{
				jsonStream.close();
			}
		}
		finally{
			response.close();
		} 
		
		return resources;
	}
	
	
	public KorapResult query(String query, QueryLanguage queryLanguage,
			String version, int startRecord, int maximumRecords,
			String[] corpora) throws HttpResponseException, IOException {
		
		checkQuery(query, startRecord, maximumRecords);
		
		HttpUriRequest httpRequest = null;	
		
		/*if (corpora != null){
			// create virtual collection			
			logger.info("Select collection");
			CollectionQuery collectionQuery = new CollectionQuery()
            .addMetaFilter("corpusID", DEFAULT_COLLECTION);
			
			logger.info("create JsonLD");
			QuerySerializer ss = new QuerySerializer()
	            .setQuery(query, QUERY_LANGUAGE,version)
	            .setCollection(collectionQuery)
	            .setMeta(CONTEXT_TYPE, CONTEXT_TYPE, 
	            		CONTEXT_SIZE, CONTEXT_SIZE, 5, startRecord-1);
			
			String jsonld=ss.build(); 
			logger.info(jsonld);
			
			HttpPost post = new HttpPost(SERVICE_URI+"_raw");
			post.setEntity(new StringEntity(jsonld));
			httpRequest = post;		
		}
		else {*/
		
			try {
			httpRequest = createRequest(query, queryLanguage, version,
					startRecord - 1,
						maximumRecords);
			} catch (URISyntaxException e) {
				throw new IOException("Failed creating http request.");
			}
		//}	
			
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		KorapResult result = null;
		try {
			response = client.execute(httpRequest);
			
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK){
				logger.warn("Error response code: "+statusCode);
				logger.warn("Error message: "+response.getStatusLine().getReasonPhrase());				
				String[] errorMsg = parseError(response);
				logger.warn(errorMsg[0] +"#" +errorMsg[1]);
				throw new HttpResponseException(Integer.parseInt(errorMsg[0]), 
						errorMsg[1]);
			}
			
			BufferedInputStream jsonStream = new BufferedInputStream( 
					response.getEntity().getContent() );	
			try {				
				result = objectMapper.readValue(jsonStream, KorapResult.class);
			} catch (IOException e) {
				throw new IOException("Failed processing response.");
			}
			finally{
				jsonStream.close();
			}		
		}	
		finally{
			response.close();
		}
		
		return result;
	}	
	
	private String[] parseError(CloseableHttpResponse response) 
			throws IOException{
		InputStream is = response.getEntity().getContent(); 
		JsonNode node = objectMapper.readTree(is);
		String message = node.get("error").textValue();
		String[] errorItems;
		if (message.contains("SRU diagnostic")) {
			errorItems = message.split(":", 2);
			errorItems[0] = errorItems[0].replace("SRU diagnostic ", "");
			errorItems[1] = errorItems[1].trim();
		}
		else if (message.contains("not a supported query language")){
			errorItems = new String[]{"4",
					"KorAP does not support the query language."};
		}
		else {
			errorItems = new String[]{"1", message};
		}
		
		return errorItems;						
	}
	
	private HttpGet createRequest(String query, QueryLanguage queryLanguage,
			String version, int startRecord, int maximumRecords)
			throws URISyntaxException {

		if (maximumRecords <= 0) {
			maximumRecords = defaultNumOfRecords;
        } else if (maximumRecords > defaultMaxRecords) {
            logger.info("limit truncated from {} to {}", maximumRecords, 
            		defaultMaxRecords);
            maximumRecords = defaultMaxRecords;
        }		
		
		List<NameValuePair> params = new ArrayList<NameValuePair>(); 
		params.add(new BasicNameValuePair("q", query));
		params.add(new BasicNameValuePair("ql", queryLanguage.toString()));
		params.add(new BasicNameValuePair("v", version));
		params.add(new BasicNameValuePair("context", CONTEXT_TYPE));
		params.add(new BasicNameValuePair("count", String.valueOf(maximumRecords)));
		params.add(new BasicNameValuePair("offset", String.valueOf(startRecord)));
		
		URIBuilder builder = new URIBuilder(SERVICE_URI + "search");
		builder.addParameters(params);	
		URI uri = builder.build();
		logger.info("Query URI: "+ uri.toString());
		HttpGet request = new HttpGet(uri);
		return request;
	}

	private void checkQuery(String query, int startRecord, int maxRecord) {		   
        if (query == null) {
            throw new NullPointerException("Query == null.");
        }
        if (query.isEmpty()) {
            throw new IllegalArgumentException("Query is empty.");
        }
        if (startRecord < 1) {
            throw new IllegalArgumentException("Start record begins from 1.");
        }
        if (maxRecord < 1) {
            throw new IllegalArgumentException("Maximum records is too low.");
        }	
	}
}
