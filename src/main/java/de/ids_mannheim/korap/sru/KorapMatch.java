package de.ids_mannheim.korap.sru;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KorapMatch {

    private String matchID;
    private String positionId;
    private String docId;
    private String textId;
    private String corpusId;
    private String leftContext;
    private String keyword;
    private String rightContext;
    private String snippet;
    private String text;

    private List<AnnotationLayer> annotationLayers = new ArrayList<AnnotationLayer>();
    
    private static Pattern idPattern = Pattern.compile("match-(.*)/(.*)/(.*)-p([0-9]+-[0-9]+.*)");

    public KorapMatch () {}
    
    @JsonProperty("matchID")
    public String getMatchId() {
        return matchID;
    }

    public void setMatchId(String id) {
        this.matchID = id;
    }
   
    public void parseMatchId(){
        Matcher matcher = idPattern.matcher(matchID);
        if (matcher.find()){
            this.corpusId = matcher.group(1);
            this.docId = matcher.group(2);
            this.setTextId(matcher.group(3));
            this.positionId = "p"+matcher.group(4);
        }
    } 
    
    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getPositionId() {
        return positionId;
    }

    public String getTextId () {
        return textId;
    }

    public void setTextId (String textId) {
        this.textId = textId;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docID) {
        this.docId = docID;
    }

    public String getCorpusId() {
        return corpusId;
    }

    public void setCorpusId(String corpusId) {
        this.corpusId = corpusId;
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
