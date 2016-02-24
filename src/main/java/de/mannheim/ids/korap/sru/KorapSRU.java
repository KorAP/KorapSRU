package de.mannheim.ids.korap.sru;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLTermNode;

import com.fasterxml.jackson.databind.JsonNode;

import eu.clarin.sru.server.SRUConfigException;
import eu.clarin.sru.server.SRUConstants;
import eu.clarin.sru.server.SRUDiagnosticList;
import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.SRURequest;
import eu.clarin.sru.server.SRUSearchResultSet;
import eu.clarin.sru.server.SRUServerConfig;
import eu.clarin.sru.server.SRUVersion;
import eu.clarin.sru.server.fcs.ResourceInfo;
import eu.clarin.sru.server.fcs.ResourceInfoInventory;
import eu.clarin.sru.server.fcs.SimpleEndpointSearchEngineBase;
import eu.clarin.sru.server.fcs.XMLStreamWriterHelper;
import eu.clarin.sru.server.fcs.utils.SimpleResourceInfoInventory;

/**
 * @author margaretha
 * */
public class KorapSRU extends SimpleEndpointSearchEngineBase{

//	private static final String RESOURCE_INFO_INVENTORY_URL =
//            "/WEB-INF/resource-info.xml";
	private static final String CLARIN_FCS_RECORD_SCHEMA =
            "http://clarin.eu/fcs/1.0";
	private static final String KORAP_WEB_URL = 
			"http://korap.ids-mannheim.de/app/#!login";
	
	private KorapClient korapClient;
	private SRUServerConfig serverConfig;
	private String redirectBaseURI;
	
	private Logger logger = (Logger) LoggerFactory.getLogger(KorapSRU.class);	
	
	@Override
	protected ResourceInfoInventory createResourceInfoInventory(
			ServletContext context, SRUServerConfig config,
			Map<String, String> params) throws SRUConfigException {
		
		List<ResourceInfo> resourceList = new ArrayList<ResourceInfo>();
		
		List<String> languages = new ArrayList<String>();
		languages.add("deu");
		
		/*Locale locale = new Locale.Builder().setRegion("DE").build();
		for (Locale l : locale.getAvailableLocales()){
			if (l.getCountry().equals("DE"))
				logger.info("locale "+l.getISO3Language());
		}*/
		
		Map<String,String> title;
		Map<String,String> description;
		try {
			JsonNode resources = korapClient.retrieveResources();
			for (JsonNode r : resources){
				title = new HashMap<String,String>();				
				title.put("de", r.get("name").asText());
				title.put("en", r.get("name").asText());
				
				description = new HashMap<String,String>();
				description.put("de", r.get("description").asText());
				
				ResourceInfo ri = new ResourceInfo(
						r.get("id").asText(), -1, title, description, 
						KORAP_WEB_URL, languages , null);
				resourceList.add(ri);
			}
		
		} catch (Exception e) {
			throw new SRUConfigException(
                    "error initializing resource info inventory", e);
		}		
		return new SimpleResourceInfoInventory(resourceList, false);
	}

	@Override
	protected void doInit(ServletContext context, SRUServerConfig config,
			Map<String, String> params) throws SRUConfigException {
		
		serverConfig = config;
		korapClient = new KorapClient(config.getNumberOfRecords(), 
				config.getMaximumRecords());
		
		StringBuilder sb = new StringBuilder();
		sb.append(config.getTransports());
		sb.append("://");
        sb.append(config.getHost());
        if (config.getPort() != 80) {
            sb.append(":").append(config.getPort());
        }
        sb.append("/").append(config.getDatabase());
        sb.append("/").append("redirect/");
        this.redirectBaseURI = sb.toString();
	}
	
	private Map<String, String> createLocaleMap(){
		// country,language
		Map<String, String> map = new HashMap<String,String>();
		Locale locale = new Locale("en");		
		for (String country : locale.getISOCountries()){
			for (Locale l : locale.getAvailableLocales()){
				if (l.getCountry().equals(country)){
					map.put(country, l.getISO3Language());
				}
			}	
		}
		return map;
	}
	
	@Override
	public SRUSearchResultSet search(SRUServerConfig config,
			SRURequest request, SRUDiagnosticList diagnostics)
			throws SRUException {
				
		checkSchema(request);		
		
        String korapQuery = translateCQLtoKorapQuery(request.getQuery());
        String version = null;
        if (request.isVersion(SRUVersion.VERSION_1_1)){
        	 version = "1.1";
        }
        else if (request.isVersion(SRUVersion.VERSION_1_2)){
        	version = "1.2";
        }
        else {
        	serverConfig.getDefaultVersion();
        }
       
		KorapResult result = null;
		try {
			result = korapClient.query(
					korapQuery,
					version,
					request.getStartRecord(), 
					request.getMaximumRecords(),
					getCorporaList(request)
			);
		}
		catch (HttpResponseException e) {
			logger.warn("HttpResponseException: " +e.getStatusCode()+" "+e.getMessage());
			switch (e.getStatusCode()) {
			case 16:
				throw new SRUException(SRUConstants.SRU_UNSUPPORTED_INDEX, 
						e.getMessage());
			case 19:
				throw new SRUException(SRUConstants.SRU_UNSUPPORTED_RELATION, 
						e.getMessage());	
			case 20: 
				throw new SRUException(SRUConstants.SRU_UNSUPPORTED_RELATION_MODIFIER,
						e.getMessage());			
			case 27: 
				throw new SRUException(SRUConstants.SRU_EMPTY_TERM_UNSUPPORTED,
						e.getMessage());
			case 48 :
				throw new SRUException(SRUConstants.SRU_QUERY_FEATURE_UNSUPPORTED, 
						e.getMessage());
			default: 
				throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR);
			}
			
		}
		catch (Exception e) {
			throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR);
		}		
		
		return generateSRUResultSet(diagnostics, result); 
	}
	
	private String[] getCorporaList(SRURequest request){
		try { 
			String corpusPids = request.getExtraRequestData("x-fcs-context");
			if (!corpusPids.isEmpty() && corpusPids != null){
				if (corpusPids.contains(",")){
					return corpusPids.split(",");
				}
				return new String[]{corpusPids};
			}
			return null;
		}
		catch (NullPointerException e) {
			return null;
		}
	}
	

	private String translateCQLtoKorapQuery(CQLNode query) throws SRUException {		
		String queryStr = query.toString();
		if ((queryStr == null) || queryStr.isEmpty()) {
            throw new SRUException(SRUConstants.SRU_EMPTY_TERM_UNSUPPORTED,
                    "An empty term is not supported.");
        }
		return queryStr;
	}
	
	private void checkSchema(SRURequest request) throws SRUException{
		final String recordSchemaIdentifier =
	            request.getRecordSchemaIdentifier();
	        if ((recordSchemaIdentifier != null) &&
	                !recordSchemaIdentifier.equals(CLARIN_FCS_RECORD_SCHEMA)) {
	            throw new SRUException(
	                    SRUConstants.SRU_UNKNOWN_SCHEMA_FOR_RETRIEVAL,
	                    recordSchemaIdentifier, "Record schema \"" +
	                    recordSchemaIdentifier +
	                    "\" is not supported by this endpoint.");
	        }
	}
		
	private SRUSearchResultSet generateSRUResultSet(SRUDiagnosticList diagnostics,
			final KorapResult result) {
		
		return new SRUSearchResultSet(diagnostics) {
			
			private int i = -1;
			
			@Override
			public void writeRecord(XMLStreamWriter writer) throws XMLStreamException {
				KorapMatch match = result.getMatch(i);				
				XMLStreamWriterHelper.writeResourceWithKWICDataView(writer,
						match.getDocID(), redirectBaseURI + match.getDocID(),
						match.getLeftContext(), match.getKeyword(),
						match.getRightContext()
					);
//				FCSResultWriter.writeResource(writer,
//					// pid, ref
//					"", redirectBaseURI + match.getDocID(),
//					match.getLeftContext(), match.getKeyword(),
//					match.getRightContext()
//				);
				//logger.info("left {}, key {}, right {} ", match.getLeftContext(), match.getKeyword(), match.getRightContext());
			}
			
			@Override
			public boolean nextRecord() throws SRUException {
				return (++i < result.getSize() ? true : false );
			}
			
			@Override
			public int getTotalRecordCount() {
				return result.getTotalResults();
			}
			
			@Override
			public String getRecordSchemaIdentifier() {
				return CLARIN_FCS_RECORD_SCHEMA;
			}
			
			@Override
			public String getRecordIdentifier() {
				return null;
			}
			
			@Override
			public int getRecordCount() {
				return result.getSize();
			}
		};
	}
}
