package de.mannheim.ids.korap.sru;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KorapResult {
	private int totalResults;
	private List<KorapMatch> matches;
	
	public KorapResult() {
		matches = new ArrayList<KorapMatch>();
	}
	
	public int getTotalResults() {
		return totalResults;
	}
	public List<KorapMatch> getMatches() {
		return matches;
	}
	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}
	public void setMatches(List<KorapMatch> matches) {
		this.matches = matches;
	}
	
	public KorapMatch getMatch(int i){
		if (i>=0 && i< getMatchSize())
			return matches.get(i);
		
		return null;
	}
	
	public int getMatchSize(){
		return matches.size();
	}
}
