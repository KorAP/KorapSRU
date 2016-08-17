package de.mannheim.ids.korap.sru;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler class for parsing the match snippet and extracting the
 * annotations from Korap MatchInfo service.
 * 
 * @author margaretha
 * 
 */
public class AnnotationHandler extends DefaultHandler {

    private Logger logger = (Logger) LoggerFactory
            .getLogger(AnnotationHandler.class);

    private boolean startSegment = true;
    private boolean startSentence = false;

    private int matchLevel;

    private List<AnnotationLayer> annotationLayers;
    private List<String> annotationStrings;

    private StringBuilder textBuilder = new StringBuilder();
    private String text = "";

    private int id;
    long segmentStartOffset = 0, segmentEndOffset = 0;
    long textStartOffset = 0, textEndOffset = 0;

    /**
     * Constructs an AnnotationHandler for the given annotation layer
     * list.
     * 
     * @param annotationLayers
     */
    public AnnotationHandler (List<AnnotationLayer> annotationLayers) {
        this.annotationLayers = annotationLayers;
        annotationStrings = new ArrayList<String>();
        id = 1;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        // Collects the annotations within <span class="match">
        if (startSentence && attributes.getValue("title") != null
                && qName.equals("span")) {
            if (startSegment) {
                segmentStartOffset = segmentEndOffset;
                startSegment = false;
            }
            annotationStrings.add(attributes.getValue("title"));
        }
        // determine the start of collecting annotations
        else if (attributes.getValue("class") != null && qName.equals("span")) {
            if (attributes.getValue("class").equals("match")) {
                startSentence = true;
            }
            else {
                startSentence = false;
            }
        }
        // add a text segment to the text layer
        else if (qName.equals("mark")) {
            text = textBuilder.toString();
            textBuilder = new StringBuilder();
            if (!text.isEmpty()) {
                addAnnotationToMap(text, annotationLayers.get(0), matchLevel,
                        textStartOffset, textEndOffset);
                textStartOffset = textEndOffset;
            }
            matchLevel++;
        }
    }

    /**
     * Parses and extracts the layer code and its value from the given
     * annotation string.
     * 
     * @param annotationStr
     */
    private void parseAnnotation(String annotationStr) {
        if (annotationStr == null || annotationStr.isEmpty()) return;

        String[] strArr = annotationStr.split(":");
        if (strArr.length < 2) return;

        String layerCode = strArr[0];
        String value = strArr[1];

        for (AnnotationLayer annotationLayer : annotationLayers) {
            if (annotationLayer.getLayerCode().equals(layerCode)
                    && !annotationLayer.getLayerCode().equals(
                            AnnotationLayer.TYPE.TEXT.toString())) {
                addAnnotationToMap(value, annotationLayer, 0);
                break;
            }
        }
    }

    private void addAnnotationToMap(String value,
            AnnotationLayer annotationLayer, int hitLevel) {
        addAnnotationToMap(value, annotationLayer, hitLevel,
                segmentStartOffset, segmentEndOffset);
    }

    private void addAnnotationToMap(String value,
            AnnotationLayer annotationLayer, int hitLevel, long startOffset,
            long endOffset) {

        Annotation annotation = new Annotation(value, startOffset, endOffset,
                hitLevel);

        Map<Integer, List<Annotation>> map = annotationLayer.getAnnotationMap();

        if (map.containsKey(id)) {
            map.get(id).add(annotation);
        }
        else {
            List<Annotation> annotations = new ArrayList<Annotation>();
            annotations.add(annotation);
            map.put(id, annotations);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        // add a text segment to the text layer
        if (qName.equals("mark")) {
            text = textBuilder.toString();
            textBuilder = new StringBuilder();
            addAnnotationToMap(text, annotationLayers.get(0), matchLevel,
                    textStartOffset, textEndOffset);
            textStartOffset = textEndOffset;
            matchLevel--;
        }
        // parses all the annotations for a span at one position.
        else if (!startSegment && qName.equals("span")) {
            for (String annotationStr : annotationStrings) {
                parseAnnotation(annotationStr);
            }
            id++;
            startSegment = true;
            annotationStrings.clear();
        }
    }

    @Override
    public void endDocument() throws SAXException {
        // add a text segment to the text layer
        text = textBuilder.toString();
        textBuilder = new StringBuilder();
        addAnnotationToMap(text, annotationLayers.get(0), matchLevel,
                textStartOffset, textEndOffset);
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        textBuilder.append(ch, start, length);
        segmentEndOffset += length;
        textEndOffset += length;
    }

    public List<AnnotationLayer> getAnnotationLayers() {
        return annotationLayers;
    }

    public void setAnnotationLayers(List<AnnotationLayer> annotationLayers) {
        this.annotationLayers = annotationLayers;
    }
}
