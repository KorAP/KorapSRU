package de.mannheim.ids.korap.sru;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KorapMatch {
	
	private String ID;
	private String docID;     
	private String leftContext;
	private String keyword;
	private String rightContext;
	private String snippet;
	private String text;
	     
	public KorapMatch() {}
	
    public KorapMatch(String source, String leftContext, String keyword,
    		String rightContext) {
    	this.docID = source;
    	this.leftContext = leftContext;
    	this.keyword = keyword;
    	this.rightContext = rightContext;
	}

	@JsonProperty("ID")
	public String getID() {
		return ID;
	}
	public void setID(String id) {
		this.ID = id;
	}

	public String getDocID() {
		return docID;
	}

	public void setDocID(String docID) {
		this.docID = docID;
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

}
