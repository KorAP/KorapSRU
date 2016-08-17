package de.mannheim.ids.korap.sru;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains properties of an annotation layer used for
 * extracting and collecting annotations, as well as generating the
 * advanced dataview in the SRU response.
 * 
 * Layer code is used to extract the annotations of the annotation
 * layer from the match snippet in a KorAP MatchInfo response.
 * 
 * Annotation map collects the extracted annotations. Since a token or
 * a text segment may have more than one annotation per annotation
 * layer, the annotations are structured as a map whose keys are ids
 * referring to the token positions.
 * 
 * @author margaretha
 * 
 */
public class AnnotationLayer {

    public static enum TYPE {
        TEXT, POS, LEMMA;

        public String toString() {
            return super.toString().toLowerCase();
        };
    }

    private String layerCode;
    private URI layerId;
    private Map<Integer, List<Annotation>> annotationMap;

    public AnnotationLayer (String layerCode, URI layerId) {
        this.layerCode = layerCode;
        this.layerId = layerId;
        this.annotationMap = new HashMap<Integer, List<Annotation>>();
    }

    public String getLayerCode() {
        return layerCode;
    }

    public void setLayerCode(String layerCode) {
        this.layerCode = layerCode;
    }

    public Map<Integer, List<Annotation>> getAnnotationMap() {
        return annotationMap;
    }

    public void setAnnotationMap(Map<Integer, List<Annotation>> annotationMap) {
        this.annotationMap = annotationMap;
    }

    public URI getLayerId() {
        return layerId;
    }

    public void setLayerId(URI layerId) {
        this.layerId = layerId;
    }
}
