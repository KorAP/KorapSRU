package de.ids_mannheim.korap.sru;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import eu.clarin.sru.server.SRUConstants;
import eu.clarin.sru.server.SRUDiagnosticList;
import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.SRUSearchResultSet;
import eu.clarin.sru.server.fcs.AdvancedDataViewWriter;
import eu.clarin.sru.server.fcs.Layer;
import eu.clarin.sru.server.fcs.XMLStreamWriterHelper;

/**
 * Prepares and creates a search result set for a search retrieve URL
 * call.
 * 
 * @author margaretha
 * 
 */
public class KorapSRUSearchResultSet extends SRUSearchResultSet {

    private Logger logger =
            LoggerFactory.getLogger(KorapSRUSearchResultSet.class);

    private int i = -1;
    private KorapResult korapResult;
    private List<String> dataviews;
    private SAXParser saxParser;
    private Layer textLayer;
    private AnnotationHandler annotationHandler;
    private KorapClient korapClient;
    private String reference;

    /**
     * Constructs a KorapSRUSearchResultSet for the given KorapResult.
     * @param korapClient 
     * 
     * @param diagnostics
     *            a list of SRU diagnostics
     * @param korapResult
     *            the query result
     * @param dataviews
     *            the required dataviews to generate
     * @param textlayer
     *            the text layer
     * @param annotationLayers
     *            the list of annotation layers
     * @throws SRUException
     */
    public KorapSRUSearchResultSet (KorapClient korapClient, SRUDiagnosticList diagnostics,
            KorapResult korapResult, List<String> dataviews, Layer textlayer,
            List<AnnotationLayer> annotationLayers) throws SRUException {
        super(diagnostics);

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {
            saxParser = saxParserFactory.newSAXParser();
        }
        catch (ParserConfigurationException | SAXException e) {
            throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR, e);
        }

        this.korapClient = korapClient;
        this.korapResult = korapResult;
        this.dataviews = dataviews;
        this.textLayer = textlayer;
        this.reference = korapResult.getResourceReference();
        annotationHandler = new AnnotationHandler(annotationLayers);
    }

    @Override
    public int getTotalRecordCount() {
        return korapResult.getTotalResults();
    }

    @Override
    public int getRecordCount() {
        return korapResult.getMatchSize();
    }

    @Override
    public String getRecordSchemaIdentifier() {
        return KorapSRU.CLARIN_FCS_RECORD_SCHEMA;
    }

    @Override
    public boolean nextRecord() throws SRUException {
        return (++i < korapResult.getMatchSize() ? true : false);
    }

    @Override
    public String getRecordIdentifier() {
        return null;
//        return korapResult.getMatch(i).getMatchId();
    }

    @Override
    public void writeRecord(XMLStreamWriter writer) throws XMLStreamException {
        KorapMatch match = korapResult.getMatch(i);
        match.parseMatchId();
        XMLStreamWriterHelper.writeStartResource(writer, match.getMatchId(),
                reference);
        XMLStreamWriterHelper.writeStartResourceFragment(writer, null, null);

        List<AnnotationLayer> annotationLayers;
        annotationLayers = parseAnnotations(match);

        writeAdvancedDataView(writer, annotationLayers);

        XMLStreamWriterHelper.writeEndResourceFragment(writer);
        XMLStreamWriterHelper.writeEndResource(writer);
    }

    /**
     * Parses the current match snippet from KorAP search API into
     * keyword, left context and right context.
     * 
     * @return a KorapMatch
     * @throws XMLStreamException
     */
    @Deprecated
    private KorapMatch parseMatch() throws XMLStreamException {
        KorapMatch match = korapResult.getMatch(i);
        String snippet = "<snippet>" + match.getSnippet() + "</snippet>";
        InputStream is = new ByteArrayInputStream(snippet.getBytes());
        try {
            saxParser.parse(is, new KorapMatchHandler(match));
        }
        catch (SAXException | IOException e) {
            throw new XMLStreamException(e);
        }
        return match;
    }

    /**
     * Retrieves and parses the annotations of a match from KorAP
     * MatchInfo API.
     * 
     * @param match
     *            a KorapMatch
     * @return a list of annotation layers containing the match
     *         annotations.
     * @throws XMLStreamException
     */
    private List<AnnotationLayer> parseAnnotations(KorapMatch match)
            throws XMLStreamException {
        if (match == null) {
            throw new NullPointerException("KorapMatch is null.");
        }

        try {
            String annotationSnippet = korapClient.retrieveAnnotations(
                    match.getCorpusId(), match.getDocId(), match.getTextId(),
                    match.getPositionId(), "*");
            InputStream is = new ByteArrayInputStream(
                    annotationSnippet.getBytes("UTF-8"));
            saxParser.parse(is, annotationHandler);
        }
        catch (SAXException | IOException | URISyntaxException e) {
            logger.error(e.getMessage());
            throw new XMLStreamException(e);
        }

        return annotationHandler.getAnnotationLayers();
    }

    /**
     * Writes advanced data views, namely segment annotations for each
     * annotation layer.
     * 
     * @param writer
     *            an XMLStreamWriter
     * @param annotationLayers
     *            a list of annotation layers
     * @throws XMLStreamException
     */
    private void writeAdvancedDataView(XMLStreamWriter writer,
            List<AnnotationLayer> annotationLayers) throws XMLStreamException {

        AdvancedDataViewWriter helper = new AdvancedDataViewWriter(
                AdvancedDataViewWriter.Unit.ITEM);

        addAnnotationsToWriter(helper, annotationLayers);

        helper.writeHitsDataView(writer, textLayer.getResultId());

        if (dataviews.contains("adv")) {
            helper.writeAdvancedDataView(writer);
        }
        helper.reset();
    }

    /**
     * Adds all annotations to the AdvancedDataViewWriter.
     * 
     * @param helper
     *            an AdvancedDataViewWriter
     * @param annotationLayers
     *            a list of annotation layers containing match
     *            annotations.
     */
    private void addAnnotationsToWriter(AdvancedDataViewWriter helper,
            List<AnnotationLayer> annotationLayers) {

        Map<Integer, List<Annotation>> map;
        for (AnnotationLayer annotationLayer : annotationLayers) {
            map = annotationLayer.getAnnotationMap();
            Set<Integer> keyset = map.keySet();
            Integer[] keyArray = keyset.toArray(new Integer[keyset.size()]);
            Arrays.sort(keyArray);
            for (int key : keyArray) {
                List<Annotation> annotations = map.get(key);
                if (annotations == null) {
                    continue;
                }

                
                 for (Annotation annotation : annotations){
                    try {
                        if (annotation.getHitLevel() > 0) {
                            helper.addSpan(annotationLayer.getLayerId(),
                                    annotation.getStart(), annotation.getEnd(),
                                    annotation.getValue(), annotation.getHitLevel());
                        }
                        else {
                            helper.addSpan(annotationLayer.getLayerId(),
                                    annotation.getStart(), annotation.getEnd(),
                                    annotation.getValue());
                        }
                    }
                    catch (Exception e) {
                        logger.error(e.getMessage());
                        continue;
                    }
                    
                    // FCS advanced dataview does not allow multiple
                    // annotations on the same segment.
                    if (!annotationLayer.getLayerCode().equals("text")){
                        break;
                    }
                }
            }
            map.clear();
        }
    }
}
