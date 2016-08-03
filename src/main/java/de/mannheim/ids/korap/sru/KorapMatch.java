package de.mannheim.ids.korap.sru;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KorapMatch {

    private String ID;
    private String positionID;
    private String docID;
    private String corpusID;
    private String leftContext;
    private String keyword;
    private String rightContext;
    private String snippet;
    private String text;

    private List<AnnotationLayer> annotationLayers = new ArrayList<AnnotationLayer>();

    public KorapMatch () {}

    @JsonProperty("ID")
    public String getID() {
        return ID;
    }

    public void setID(String id) {
        this.ID = id;
    }

    public void setPositionID() {
        String[] idParts = ID.split("-");
        this.positionID = idParts[2] + "-" + idParts[3];
    }

    public String getPositionID() {
        return positionID;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID.replace(corpusID + "_", "");
    }

    public String getCorpusID() {
        return corpusID;
    }

    public void setCorpusID(String corpusID) {
        this.corpusID = corpusID;
    }

    public String getLeftContext() {
        return leftContext;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getRightContext() {
        return rightContext;
    }

    public void setLeftContext(String leftContext) {
        this.leftContext = leftContext;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setRightContext(String rightContext) {
        this.rightContext = rightContext;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<AnnotationLayer> getAnnotationLayers() {
        return annotationLayers;
    }

    public void setAnnotationLayers(List<AnnotationLayer> annotationLayers) {
        this.annotationLayers = annotationLayers;
    }

}
