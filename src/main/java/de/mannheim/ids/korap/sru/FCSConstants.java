package de.mannheim.ids.korap.sru;

public class FCSConstants {

    private static final String FCS_DIAGNOSTIC_URI_PREFIX = "http://clarin.eu/fcs/diagnostic/";

    public static final String FCS_PERSISTENT_IDENTIFIER_INVALID = 
            FCS_DIAGNOSTIC_URI_PREFIX + 1;
    public static final String FCS_RESOURCE_SET_TOO_LARGE_QUERY_CONTEXT_AUTOMATICALLY_ADJUSTED = 
            FCS_DIAGNOSTIC_URI_PREFIX + 2;
    public static final String FCS_RESOURCE_SET_TOO_LARGE_CANNOT_PERFORM_QUERY = 
            FCS_DIAGNOSTIC_URI_PREFIX + 3;
    public static final String FCS_REQUESTED_DATAVIEW_INVALID = 
            FCS_DIAGNOSTIC_URI_PREFIX + 4;
    public static final String FCS_GENERAL_QUERY_SYNTAX_ERROR = 
            FCS_DIAGNOSTIC_URI_PREFIX + 10;
    public static final String FCS_QUERY_TOO_COMPLEX =
            FCS_DIAGNOSTIC_URI_PREFIX + 11;
    public static final String FCS_QUERY_REWRITTEN = 
            FCS_DIAGNOSTIC_URI_PREFIX + 12;
    public static final String FCS_GENERAL_PROCESSING_HINT = 
            FCS_DIAGNOSTIC_URI_PREFIX + 14;
}
