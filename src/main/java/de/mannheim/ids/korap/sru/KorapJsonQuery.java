package de.mannheim.ids.korap.sru;

public class KorapJsonQuery {	
	
	String collection;
	String query, queryLanguage, version;
	String lCtx, rCtx;
	int lCtxs, rCtxs;
	int startIndex, pageLength;
	
	public KorapJsonQuery(String query, String collection, String lang, 
			String version, int lCtxs, int rCtxs, int startIndex, int pageLength) {
		this(query, collection, lang, version, "token", "token", lCtxs, rCtxs, 
				startIndex, pageLength);
	}
	
	public KorapJsonQuery(String query, String collection, String queryLanguage, 
			String version, String lCtx, String rCtx, int lCtxs, int rCtxs, 
			int startIndex, int pageLength) {
		this.query = query;
		this.collection = collection;
		this.queryLanguage = queryLanguage;
		this.version = version;
		this.lCtx = lCtx;
		this.rCtx = rCtx;
		this.lCtxs = lCtxs;
		this.rCtxs = rCtxs;
		this.startIndex = startIndex;
		this.pageLength = pageLength;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQueryLanguage() {
		return queryLanguage;
	}

	public void setQueryLanguage(String queryLanguage) {
		this.queryLanguage = queryLanguage;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getlCtx() {
		return lCtx;
	}

	public void setlCtx(String lCtx) {
		this.lCtx = lCtx;
	}

	public String getrCtx() {
		return rCtx;
	}

	public void setrCtx(String rCtx) {
		this.rCtx = rCtx;
	}

	public int getlCtxs() {
		return lCtxs;
	}

	public void setlCtxs(int lCtxs) {
		this.lCtxs = lCtxs;
	}

	public int getrCtxs() {
		return rCtxs;
	}

	public void setrCtxs(int rCtxs) {
		this.rCtxs = rCtxs;
	}

	public int getPageLength() {
		return pageLength;
	}

	public void setPageLength(int pageLength) {
		this.pageLength = pageLength;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
	
}
