package de.ids_mannheim.korap.sru;

public enum QueryLanguage {
	CQL, FCSQL;
	
	public String toString() {
	    return super.toString().toLowerCase();
	};
}
