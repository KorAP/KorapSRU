package de.ids_mannheim.korap.sru;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KorapResource {

    private String resourceId;
    private Map<String, String> titles;
    private String description;
    private String[] languages;
    private Map<Integer, String> layers;
    private String institution;
    private String landingPage;
    
    public String getResourceId () {
        return resourceId;
    }
    public void setResourceId (String resourceId) {
        this.resourceId = resourceId;
    }
    public Map<String, String> getTitles () {
        return titles;
    }
    public void setTitles (Map<String, String> titles) {
        this.titles = titles;
    }
    public String getDescription () {
        return description;
    }
    public void setDescription (String description) {
        this.description = description;
    }
    public String[] getLanguages () {
        return languages;
    }
    public void setLanguages (String[] languages) {
        this.languages = languages;
    }
    public Map<Integer, String> getLayers () {
        return layers;
    }
    public void setLayers (Map<Integer, String> layers) {
        this.layers = layers;
    }
	public String getInstitution () {
		return institution;
	}
	public void setInstitution (String institution) {
		this.institution = institution;
	}
	public String getLandingPage () {
		return landingPage;
	}
	public void setLandingPage (String landingPage) {
		this.landingPage = landingPage;
	}

}
