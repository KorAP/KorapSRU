package de.mannheim.ids.korap.sru;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.server.SRUConfigException;
import eu.clarin.sru.server.SRUConstants;
import eu.clarin.sru.server.SRUDiagnosticList;
import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.SRUQueryParserRegistry;
import eu.clarin.sru.server.SRURequest;
import eu.clarin.sru.server.SRUSearchResultSet;
import eu.clarin.sru.server.SRUServerConfig;
import eu.clarin.sru.server.fcs.Constants;
import eu.clarin.sru.server.fcs.DataView;
import eu.clarin.sru.server.fcs.EndpointDescription;
import eu.clarin.sru.server.fcs.SimpleEndpointSearchEngineBase;

/**
 * @author margaretha
 * */
public class KorapSRU extends SimpleEndpointSearchEngineBase {

	public static final String CLARIN_FCS_RECORD_SCHEMA = "http://clarin.eu/fcs/resource";
	public static final String KORAP_WEB_URL = "http://korap.ids-mannheim.de/kalamar";

	public static String redirectBaseURI;
	public static KorapClient korapClient;
	private KorapEndpointDescription korapEndpointDescription;
	private SRUServerConfig serverConfig;

	private Logger logger = (Logger) LoggerFactory.getLogger(KorapSRU.class);

	@Override
	protected EndpointDescription createEndpointDescription(
			ServletContext context, SRUServerConfig config,
			Map<String, String> params) throws SRUConfigException {
		korapEndpointDescription = new KorapEndpointDescription(context);
		return korapEndpointDescription;
	}

	@Override
	protected void doInit(ServletContext context, SRUServerConfig config,
			SRUQueryParserRegistry.Builder parserRegistryBuilder,
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

		// queryParser = new ThreadLocal<QueryParser>() {
		// @Override
		// protected QueryParser initialValue() {
		// return new QueryParser("text", new StandardAnalyzer(
		// CharArraySet.EMPTY_SET));
		// }
		// };
	}

	@Override
	public SRUSearchResultSet search(SRUServerConfig config,
			SRURequest request, SRUDiagnosticList diagnostics)
			throws SRUException {

		checkRequestRecordSchema(request);

		String dataview = korapEndpointDescription.getDefaultDataView();
		if (request.getExtraRequestDataNames().contains("x-fcs-dataviews")) {
			dataview = getRequestDataView(
					request.getExtraRequestData("x-fcs-dataviews"), diagnostics);
		}

		String queryType = request.getQueryType();
		logger.info("Query language: " + queryType);
		QueryLanguage queryLanguage;
		if (request.isQueryType(Constants.FCS_QUERY_TYPE_CQL)) {
			queryLanguage = QueryLanguage.CQL;
		} else if (request.isQueryType(Constants.FCS_QUERY_TYPE_FCS)) {
			queryLanguage = QueryLanguage.FCSQL;
		} else {
			throw new SRUException(
					SRUConstants.SRU_CANNOT_PROCESS_QUERY_REASON_UNKNOWN,
					"Queries with queryType '"
							+ request.getQueryType()
							+ "' are not supported by this CLARIN-FCS Endpoint.");
		}
		logger.info("Query language: " + queryLanguage);

		String queryStr = null;
		queryStr = request.getQuery().getRawQuery();
		if ((queryStr == null) || queryStr.isEmpty()) {
			throw new SRUException(SRUConstants.SRU_EMPTY_TERM_UNSUPPORTED,
					"An empty term is not supported.");
		}
		logger.info("korapsru query: " + queryStr);

		String version = null;
		switch (request.getVersion()) {
			case VERSION_1_1 :
				version = "1.1";
			case VERSION_1_2 :
				version = "1.2";
			case VERSION_2_0 :
				version = "2.0";
			default :
				serverConfig.getDefaultVersion();
		}

		KorapResult korapResult = new KorapResult();
		try {
			korapResult = korapClient.query(queryStr, queryLanguage, version,
					request.getStartRecord(), request.getMaximumRecords(),
					getCorporaList(request));
		} catch (HttpResponseException e) {
			logger.warn("HttpResponseException: " + e.getStatusCode() + " "
					+ e.getMessage());
			switch (e.getStatusCode()) {
				case 16 :
					throw new SRUException(SRUConstants.SRU_UNSUPPORTED_INDEX,
							e.getMessage());
				case 19 :
					throw new SRUException(
							SRUConstants.SRU_UNSUPPORTED_RELATION,
							e.getMessage());
				case 20 :
					throw new SRUException(
							SRUConstants.SRU_UNSUPPORTED_RELATION_MODIFIER,
							e.getMessage());
				case 27 :
					throw new SRUException(
							SRUConstants.SRU_EMPTY_TERM_UNSUPPORTED,
							e.getMessage());
				case 48 :
					throw new SRUException(
							SRUConstants.SRU_QUERY_FEATURE_UNSUPPORTED,
							e.getMessage());
				default :
					throw new SRUException(
							SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
							e.getMessage());
			}

		} catch (IOException e) {
			throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
					e.getMessage());
		}

		return new KorapSRUSearchResultSet(diagnostics, korapResult, dataview);
	}

	private String[] getCorporaList(SRURequest request) {
		try {
			String corpusPids = request.getExtraRequestData("x-fcs-context");
			if (!corpusPids.isEmpty() && corpusPids != null) {
				if (corpusPids.contains(",")) {
					return corpusPids.split(",");
				}
				return new String[]{corpusPids};
			}
			return null;
		} catch (NullPointerException e) {
			return null;
		}
	}

	private void checkRequestRecordSchema(SRURequest request)
			throws SRUException {
		final String recordSchemaIdentifier = request
				.getRecordSchemaIdentifier();
		if ((recordSchemaIdentifier != null)
				&& !recordSchemaIdentifier.equals(CLARIN_FCS_RECORD_SCHEMA)) {
			throw new SRUException(
					SRUConstants.SRU_UNKNOWN_SCHEMA_FOR_RETRIEVAL,
					recordSchemaIdentifier, "Record schema \""
							+ recordSchemaIdentifier
							+ "\" is not supported by this endpoint.");
		}
	}

	private String getRequestDataView(String requestDataview,
			SRUDiagnosticList diagnostics) {
		if (requestDataview != null & !requestDataview.isEmpty()) {
			for (DataView dv : korapEndpointDescription.getSupportedDataViews()) {
				if (dv.getIdentifier().equals(requestDataview)) {
					return requestDataview;
				}
			}
			diagnostics.addDiagnostic(
					Constants.FCS_DIAGNOSTIC_REQUESTED_DATA_VIEW_INVALID,
					"The requested Data View " + requestDataview
							+ " is not supported.", "The default Data View "
							+ korapEndpointDescription.getDefaultDataView()
							+ " is used.");
		}
		return korapEndpointDescription.getDefaultDataView();
	}
}
