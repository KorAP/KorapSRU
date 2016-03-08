package de.mannheim.ids.korap.sru;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import eu.clarin.sru.server.SRUConfigException;
import eu.clarin.sru.server.SRUConstants;
import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.fcs.DataView;
import eu.clarin.sru.server.fcs.DataView.DeliveryPolicy;
import eu.clarin.sru.server.fcs.EndpointDescription;
import eu.clarin.sru.server.fcs.Layer;
import eu.clarin.sru.server.fcs.ResourceInfo;

public class KorapEndpointDescription implements EndpointDescription {

	private List<DataView> dataviews;
	private List<URI> capabilities;
	private List<String> languages;
	
	private String defaultDataview = "hits";

	public KorapEndpointDescription() throws SRUConfigException {
		dataviews = new ArrayList<DataView>();
		dataviews.add(new DataView("hits", "application/x-clarin-fcs-hits+xml",
				DeliveryPolicy.SEND_BY_DEFAULT));
		dataviews.add(new DataView("kwic", "application/x-clarin-fcs-kwic+xml",
				DeliveryPolicy.NEED_TO_REQUEST));

		capabilities = new ArrayList<URI>();
		try {
			capabilities.add(new URI(
					"http://clarin.eu/fcs/capability/basic-search"));
		} catch (URISyntaxException e) {
			throw new SRUConfigException("Found an invalid capability URI.");
		}

		languages = new ArrayList<String>();
		languages.add("deu");
	}

	@Override
	public void destroy() {
		dataviews.clear();
		capabilities.clear();
		languages.clear();
	}

	@Override
	public List<URI> getCapabilities() {
		return capabilities;
	}

	@Override
	public List<DataView> getSupportedDataViews() {
		return dataviews;
	}

	@Override
	public List<ResourceInfo> getResourceList(String pid) throws SRUException {

		List<ResourceInfo> resourceList = new ArrayList<ResourceInfo>();
		
		Map<String,String> title;
		Map<String,String> description;

		JsonNode resources;

		try {
			resources = KorapSRU.korapClient.retrieveResources();
		} catch (URISyntaxException | IOException e) {
			throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
					"Failed retrieving resources.");
		}

		for (JsonNode r : resources) {
			title = new HashMap<String, String>();
			title.put("de", r.get("name").asText());
			title.put("en", r.get("name").asText());

			description = new HashMap<String, String>();
			description.put("de", r.get("description").asText());

			ResourceInfo ri = new ResourceInfo(r.get("id").asText(), title,
					description, KorapSRU.KORAP_WEB_URL, languages, dataviews,
					this.getSupportedLayers(), null);
			resourceList.add(ri);
		}
		
		return resourceList;
	}

	public String getDefaultDataView() {
		return defaultDataview;
	}

	public void setDefaultDataView(String defaultDataview) {
		this.defaultDataview = defaultDataview;
	}

	@Override
	public List<Layer> getSupportedLayers() {
		return null;
	}

}
