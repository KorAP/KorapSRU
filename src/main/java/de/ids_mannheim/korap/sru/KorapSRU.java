package de.ids_mannheim.korap.sru;

import java.io.IOException;
import java.util.List;
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
import eu.clarin.sru.server.SRUVersion;
import eu.clarin.sru.server.fcs.Constants;
import eu.clarin.sru.server.fcs.DataView;
import eu.clarin.sru.server.fcs.EndpointDescription;
import eu.clarin.sru.server.fcs.SimpleEndpointSearchEngineBase;

/**
 * KorAP search engine endpoint implementation supporting SRU calls
 * with operation explain and search retrieve.
 * 
 * @author margaretha
 * */
public class KorapSRU extends SimpleEndpointSearchEngineBase {

    public static final String CLARIN_FCS_RECORD_SCHEMA = "http://clarin.eu/fcs/resource";
    public static final String KORAP_WEB_URL = "https://korap.ids-mannheim.de/";

    public static String redirectBaseURI;
    public static KorapClient korapClient;
    private KorapEndpointDescription korapEndpointDescription;
    // private SRUServerConfig serverConfig;

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

        String korapURI = context.getInitParameter("korap.service.uri");

        korapClient = new KorapClient(korapURI,
                config.getNumberOfRecords(),
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

    @Override
    public SRUSearchResultSet search(SRUServerConfig config,
            SRURequest request, SRUDiagnosticList diagnostics)
            throws SRUException {

        checkRequestRecordSchema(request);

        List<String> dataviews = createRequestDataview(request, diagnostics);
        QueryLanguage queryLanguage = parseQueryLanguage(request);

        String queryType = request.getQueryType();
        if (!queryType.equals("fcs") && !queryType.equals("cql")){
            throw new SRUException(SRUConstants.SRU_UNSUPPORTED_PARAMETER_VALUE, 
                    "Query type "+ queryType+ " is not supported.");
        }
//        logger.info("Query language: " + queryType);
        
        SRUVersion sruVersion = request.getVersion();
        String version = parseVersion(sruVersion);
        
        String queryStr = request.getQuery().getRawQuery();
        if ((queryStr == null) || queryStr.isEmpty()) {
            throw new SRUException(SRUConstants.SRU_EMPTY_TERM_UNSUPPORTED,
                    "Empty term is not supported.");
        }
//        logger.info("korapsru query: " + queryStr);

        KorapResult korapResult = sendQuery(queryStr, request, version,
                queryLanguage);
        checkKorapResultError(korapResult, queryLanguage,
                isRewitesAllowed(request), diagnostics);
//        logger.info("Number of records: "+korapResult.getTotalResults());

        return new KorapSRUSearchResultSet(korapClient, diagnostics, korapResult, dataviews,
                korapEndpointDescription.getTextLayer(),
                korapEndpointDescription.getAnnotationLayers());
    }

    private String parseVersion(SRUVersion version) throws SRUException {
        if (version == SRUVersion.VERSION_1_1) {
            return "1.1";
        }
        else if (version == SRUVersion.VERSION_1_2) {
            return "1.2";
        }
        else if (version == SRUVersion.VERSION_2_0) {
            return "2.0";
        }
        else {
            throw new SRUException(SRUConstants.SRU_UNSUPPORTED_VERSION);
        }
    }

    private QueryLanguage parseQueryLanguage(SRURequest request)
            throws SRUException {
        if (request.isQueryType(Constants.FCS_QUERY_TYPE_CQL)) {
            return QueryLanguage.CQL;
        }
        else if (request.isQueryType(Constants.FCS_QUERY_TYPE_FCS)) {
            return QueryLanguage.FCSQL;
        }
        else {
            throw new SRUException(
                    SRUConstants.SRU_CANNOT_PROCESS_QUERY_REASON_UNKNOWN,
                    "Queries with queryType '"
                            + request.getQueryType()
                            + "' are not supported by this CLARIN-FCS Endpoint.");
        }
    }

    private boolean isRewitesAllowed(SRURequest request) {
        if (request.getExtraRequestDataNames().contains(
                "x-fcs-rewrites-allowed")) {

            String rewrites = request
                    .getExtraRequestData("x-fcs-rewrites-allowed");
            if (rewrites != null && !rewrites.isEmpty()) {

                if (rewrites.equals("true")) return true;
            }
        }
        return false;
    }

    private List<String> createRequestDataview(SRURequest request,
            SRUDiagnosticList diagnostics) {

        List<String> dataviews = korapEndpointDescription.getDefaultDataViews();

        if (request.getExtraRequestDataNames().contains("x-fcs-dataviews")) {
            String requestDataview = request
                    .getExtraRequestData("x-fcs-dataviews");
            if (requestDataview != null & !requestDataview.isEmpty()) {
                for (DataView dv : korapEndpointDescription
                        .getSupportedDataViews()) {
                    if (dv.getIdentifier().equals(requestDataview)) {
                        if (!dataviews.contains(requestDataview)){
                        	dataviews.add(requestDataview);
                        }
                        return dataviews;
                    }
                }
                diagnostics.addDiagnostic(
                        Constants.FCS_DIAGNOSTIC_REQUESTED_DATA_VIEW_INVALID,
                        "The requested Data View " + requestDataview
                                + " is not supported.",
                        "Using the default Data View(s): "
                                + korapEndpointDescription
                                        .getDefaultDataViews() + " .");
            }
        }

        return dataviews;
    }

    private KorapResult sendQuery(String queryStr, SRURequest request,
            String version, QueryLanguage queryLanguage) throws SRUException {

        try {
            return korapClient.query(queryStr, queryLanguage, version,
                    request.getStartRecord(), request.getMaximumRecords(),
                    getCorporaList(request));
        }
        catch (HttpResponseException e) {
            logger.warn("HttpResponseException: " + e.getStatusCode() + " "
                    + e.getMessage());
            switch (e.getStatusCode()) {
                case 16:
                    throw new SRUException(SRUConstants.SRU_UNSUPPORTED_INDEX,
                            e.getMessage());
                case 19:
                    throw new SRUException(
                            SRUConstants.SRU_UNSUPPORTED_RELATION,
                            e.getMessage());
                case 20:
                    throw new SRUException(
                            SRUConstants.SRU_UNSUPPORTED_RELATION_MODIFIER,
                            e.getMessage());
                case 27:
                    throw new SRUException(
                            SRUConstants.SRU_EMPTY_TERM_UNSUPPORTED,
                            e.getMessage());
                case 48:
                    throw new SRUException(
                            SRUConstants.SRU_QUERY_FEATURE_UNSUPPORTED,
                            e.getMessage());
                default:
                    throw new SRUException(
                            SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                            e.getMessage());
            }

        }
        catch (IOException e) {
            throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                    e.getMessage());
        }
    }

    private void checkKorapResultError(KorapResult korapResult,
            QueryLanguage queryLanguage, boolean isRewitesAllowed,
            SRUDiagnosticList diagnostics) throws SRUException {
        if (korapResult.getErrors() != null) {
            for (List<Object> error : korapResult.getErrors()) {
                int errorCode = (int) error.get(0);

                switch (errorCode) {
                    case 301:
                        throw new SRUException(
                                SRUConstants.SRU_EMPTY_TERM_UNSUPPORTED,
                                (String) error.get(1));
                    case 302:
                        if (queryLanguage == QueryLanguage.FCSQL) {
                            throw new SRUException(
                                    FCSConstants.FCS_GENERAL_QUERY_SYNTAX_ERROR,
                                    (String) error.get(1));
                        }
                        else {
                            throw new SRUException(
                                    SRUConstants.SRU_QUERY_SYNTAX_ERROR,
                                    (String) error.get(1));
                        }
                    case 306:
                        throw new SRUException(
                                SRUConstants.SRU_QUERY_FEATURE_UNSUPPORTED,
                                (String) error.get(1));
                    case 307:
                        throw new SRUException(
                                SRUConstants.SRU_UNSUPPORTED_PARAMETER_VALUE,
                                (String) error.get(1));
                    case 309:
                        throw new SRUException(
                                SRUConstants.SRU_MANDATORY_PARAMETER_NOT_SUPPLIED,
                                (String) error.get(1));
                    case 310:
                        throw new SRUException(
                                SRUConstants.SRU_UNSUPPORTED_VERSION,
                                (String) error.get(1));
                    case 311:
                        throw new SRUException(
                                FCSConstants.FCS_QUERY_TOO_COMPLEX,
                                (String) error.get(1));
                    case 399:
                        if (queryLanguage == QueryLanguage.FCSQL) {
                            throw new SRUException(
                                    FCSConstants.FCS_GENERAL_QUERY_SYNTAX_ERROR,
                                    (String) error.get(1));
                        }
                        else {
                            throw new SRUException(
                                    SRUConstants.SRU_QUERY_SYNTAX_ERROR,
                                    (String) error.get(1));
                        }
                    case 780:
                        throw new SRUException(
                                SRUConstants.SRU_RESULT_SET_NOT_CREATED_TOO_MANY_MATCHING_RECORDS,
                                (String) error.get(1));
                    case 781:
                        if (isRewitesAllowed) {
                            diagnostics.addDiagnostic(
                                    FCSConstants.FCS_QUERY_REWRITTEN, "",
                                    (String) error.get(1));
                        }
                        else {
                            throw new SRUException(
                                    SRUConstants.SRU_RESULT_SET_NOT_CREATED_TOO_MANY_MATCHING_RECORDS,
                                    "Too many matching records.");
                        }
                    default:
                        break;
                }

            }
        }
    }

    private String[] getCorporaList(SRURequest request) {
        try {
            String corpusPids = request.getExtraRequestData("x-fcs-context");
//            logger.info("x-fcs-context: "+corpusPids);
            if (!corpusPids.isEmpty() && corpusPids != null) {
                if (corpusPids.contains(",")) {
                    return corpusPids.split(",");
                }
                return new String[] { corpusPids };
            }
            return null;
        }
        catch (NullPointerException e) {
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

}
