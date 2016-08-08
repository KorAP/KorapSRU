package de.mannheim.ids.korap.sru;

public enum QueryLanguage {
	CQL, FCSQL;
	
	public String toString() {
	    return super.toString().toLowerCase();
	};
}
