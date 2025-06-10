package de.ids_mannheim.korap.sru;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KorapResult {
    private List<KorapMatch> matches;
    private List<List<Object>> errors;
    private KorapMeta metadata;
    private String resourceReference;

    public KorapResult () {
        matches = new ArrayList<KorapMatch>();
    }

    public int getTotalResults () {
        return metadata.getTotalResults();
    }

    public List<List<Object>> getErrors () {
        return errors;
    }

    public void setErrors (List<List<Object>> errors) {
        this.errors = errors;
    }

    public List<KorapMatch> getMatches () {
        return matches;
    }

    public void setMatches (List<KorapMatch> matches) {
        this.matches = matches;
    }

    public KorapMatch getMatch (int i) {
        if (i >= 0 && i < getMatchSize())
            return matches.get(i);

        return null;
    }

    public int getMatchSize () {
        return matches.size();
    }

    @JsonProperty("meta")
    public KorapMeta getMetadata () {
        return metadata;
    }

    public void setMetadata (KorapMeta metadata) {
        this.metadata = metadata;
    }

	public String getResourceReference () {
		return resourceReference;
	}

	public void setResourceReference (String resourceReference) {
		this.resourceReference = resourceReference;
	}
}
