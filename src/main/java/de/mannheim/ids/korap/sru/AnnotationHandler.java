package de.mannheim.ids.korap.sru;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AnnotationHandler extends DefaultHandler {

    private Logger logger = (Logger) LoggerFactory
            .getLogger(AnnotationHandler.class);

    private boolean startSegment = true;
    private boolean startSentence = false;

    private int matchLevel = 0;

    private List<AnnotationLayer> annotationLayers;
    private List<String> annotationStrings;

    private StringBuilder segmentBuilder = new StringBuilder();
    private StringBuilder textBuilder = new StringBuilder();
    private String text = "";

    private int id;
    long segmentStartOffset = 0, segmentEndOffset = 0;
    long textStartOffset = 0, textEndOffset = 0;

    public AnnotationHandler (List<AnnotationLayer> annotationLayers) {
        this.annotationLayers = annotationLayers;
        annotationStrings = new ArrayList<String>();
        id = 1;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        if (startSentence && attributes.getValue("title") != null && qName.equals("span")) {
            if (startSegment) {
                segmentStartOffset = segmentEndOffset;
                startSegment = false;
            }
            annotationStrings.add(attributes.getValue("title"));
        }
        else if (attributes.getValue("class") !=null && qName.equals("span")){            
            if (attributes.getValue("class").equals("match")){
                startSentence = true;
            }
            else {
                startSentence = false;
            }
        }
        else if (qName.equals("mark")) {
            text = textBuilder.toString();            
            textBuilder = new StringBuilder();
            if (!text.isEmpty()) {
                addAnnotationToMap(text, annotationLayers.get(0),
                        matchLevel, textStartOffset, textEndOffset);
                textStartOffset = textEndOffset;
            }
            matchLevel++;
        }
        super.startElement(uri, localName, qName, attributes);

    }

    private void parseAnnotation(String annotationStr) {
        if (annotationStr == null || annotationStr.isEmpty()) return;

        String[] strArr = annotationStr.split(":");
        if (strArr.length < 2) return;

        String layerCode = strArr[0];
        String value = strArr[1];

        for (AnnotationLayer annotationLayer : annotationLayers) {
            if (annotationLayer.getLayerCode().equals(
                    AnnotationLayer.TYPE.TEXT.toString())) {
                segmentBuilder = new StringBuilder();
            }
            else if (annotationLayer.getLayerCode().equals(layerCode)) {
                addAnnotationToMap(value, annotationLayer, 0);
                break;
            }
        }
    }

    private void addAnnotationToMap(String value,
            AnnotationLayer annotationLayer, int hitLevel) {
        addAnnotationToMap(value, annotationLayer, hitLevel, segmentStartOffset, segmentEndOffset);
    }
    
    private void addAnnotationToMap(String value,
            AnnotationLayer annotationLayer, int hitLevel, long startOffset, long endOffset) {

        Annotation annotation = new Annotation(id, value, startOffset,
                endOffset, hitLevel);

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

        if (qName.equals("mark")) {

            text = textBuilder.toString();
            textBuilder = new StringBuilder();
            addAnnotationToMap(text, annotationLayers.get(0),
                    matchLevel, textStartOffset, textEndOffset);
            textStartOffset = textEndOffset;
            matchLevel--;
        }
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
        text = textBuilder.toString();
        addAnnotationToMap(text, annotationLayers.get(0),
                matchLevel, textStartOffset, textEndOffset);
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (!startSegment) {
            segmentBuilder.append(ch, start, length);
        }        
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
