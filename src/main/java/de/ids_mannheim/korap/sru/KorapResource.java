package de.ids_mannheim.korap.sru;

import java.util.Map;

public class KorapResource {

    private String resourceId;
    private Map<String, String> titles;
    private String description;
    private String[] languages;
    private Map<Integer, String> layers;
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

}
