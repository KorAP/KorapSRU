package de.mannheim.ids.korap.sru;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KorapMeta {
    private int totalResults;

    public int getTotalResults () {
        return totalResults;
    }

    public void setTotalResults (int totalResults) {
        this.totalResults = totalResults;
    }
}
