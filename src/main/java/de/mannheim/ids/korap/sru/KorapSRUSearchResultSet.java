package de.mannheim.ids.korap.sru;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class KorapSRUSearchResultSet extends SRUSearchResultSet {

    private Logger logger = (Logger) LoggerFactory
            .getLogger(KorapSRUSearchResultSet.class);

    private int i = -1;
    private KorapResult korapResult;
    private List<String> dataviews;
    private KorapEndpointDescription endpointDescription;
    private SAXParser saxParser;

    Layer textLayer;

    public KorapSRUSearchResultSet (SRUDiagnosticList diagnostics,
            KorapResult korapResult, List<String> dataviews,
            KorapEndpointDescription korapEndpointDescription)
            throws SRUException {
        super(diagnostics);

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {
            saxParser = saxParserFactory.newSAXParser();
        }
        catch (ParserConfigurationException | SAXException e) {
            throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR, e);
        }

        this.korapResult = korapResult;
        this.dataviews = dataviews;
        this.endpointDescription = korapEndpointDescription;

        textLayer = endpointDescription.getSupportedLayers().get(0);
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
        return korapResult.getMatch(i).getMatchId();
    }

    @Override
    public void writeRecord(XMLStreamWriter writer) throws XMLStreamException {
        KorapMatch match;
        match = parseMatch();
        match.parseMatchId();

        XMLStreamWriterHelper.writeStartResource(writer, match.getMatchId(),
                null);
        XMLStreamWriterHelper.writeStartResourceFragment(writer, null, null);

        List<AnnotationLayer> annotationLayers;
        annotationLayers = parseAnnotations(match);

        writeAdvancedDataView(writer, annotationLayers);

        XMLStreamWriterHelper.writeEndResourceFragment(writer);
        XMLStreamWriterHelper.writeEndResource(writer);
    }

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

    private List<AnnotationLayer> parseAnnotations(KorapMatch match)
            throws XMLStreamException {
        AnnotationHandler annotationHandler = new AnnotationHandler(
                endpointDescription.getAnnotationLayers());
        try {
            String annotationSnippet = KorapClient.retrieveAnnotations(match);
            InputStream is = new ByteArrayInputStream(annotationSnippet.getBytes());
            saxParser.parse(is, annotationHandler);
        }
        catch (SAXException | IOException e) {
            throw new XMLStreamException(e);
        }

        return annotationHandler.getAnnotationLayers();
    }

    private void writeAdvancedDataView(XMLStreamWriter writer,
            List<AnnotationLayer> annotationLayers) throws XMLStreamException {

        AdvancedDataViewWriter helper = new AdvancedDataViewWriter(
                AdvancedDataViewWriter.Unit.ITEM);

        addAnnotationsToWriter(helper, annotationLayers);

        helper.writeHitsDataView(writer, textLayer.getResultId());

        if (dataviews.contains("adv")) {
            helper.writeAdvancedDataView(writer);
        }
    }

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

                // FCS advanced dataview does not allow multiple
                // annotations on the same segment.
                // for (Annotation annotation : annotations){
                Annotation annotation = annotations.get(0);

                // if
                // (annotationLayer.getLayerCode().equals(AnnotationLayer.TYPE.TEXT.toString())){
                // logger.info(annotation.getStart()+" "+
                // annotation.getEnd()+" "+
                // annotation.getValue());
                // }
                if (annotation.getHitLevel()>0) {
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
            map.clear();
        }
    }
}
