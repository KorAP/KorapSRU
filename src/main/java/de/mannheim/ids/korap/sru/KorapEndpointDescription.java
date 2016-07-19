package de.mannheim.ids.korap.sru;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import com.fasterxml.jackson.databind.JsonNode;

import eu.clarin.sru.server.SRUConfigException;
import eu.clarin.sru.server.SRUConstants;
import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.fcs.DataView;
import eu.clarin.sru.server.fcs.DataView.DeliveryPolicy;
import eu.clarin.sru.server.fcs.utils.SimpleEndpointDescriptionParser;
import eu.clarin.sru.server.fcs.EndpointDescription;
import eu.clarin.sru.server.fcs.Layer;
import eu.clarin.sru.server.fcs.ResourceInfo;

public class KorapEndpointDescription implements EndpointDescription {

	private List<DataView> dataviews;
	private List<URI> capabilities;
	private List<String> languages;
	
	private String defaultDataview = "hits";
	private List<Layer> layers;

	public KorapEndpointDescription(ServletContext context)
			throws SRUConfigException {
		try {
			URL url = context.getResource("/WEB-INF/endpoint-description.xml");
			EndpointDescription simpleEndpointDescription = SimpleEndpointDescriptionParser
					.parse(url);
			if (simpleEndpointDescription != null) {
				setSupportedLayers(simpleEndpointDescription
						.getSupportedLayers());
				setSupportedDataViews(simpleEndpointDescription
						.getSupportedDataViews());
				setCapabilities(simpleEndpointDescription.getCapabilities());
			}

		} catch (MalformedURLException e) {
			throw new SRUConfigException(
					"error initializing resource info inventory", e);
		}
		setLanguages();
	}

	@Override
	public void destroy() {
		dataviews.clear();
		capabilities.clear();
		languages.clear();
	}

	public void setLanguages() {
		languages = new ArrayList<String>();
		languages.add("deu");
	}

	@Override
	public List<URI> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(List<URI> list) throws SRUConfigException {
		capabilities = list;
//		new ArrayList<URI>();
//		try {
//			capabilities.add(new URI(
//					"http://clarin.eu/fcs/capability/basic-search"));
//		} catch (URISyntaxException e) {
//			throw new SRUConfigException("Found an invalid capability URI.");
//		}
	}

	@Override
	public List<DataView> getSupportedDataViews() {
		return dataviews;
	}

	public void setSupportedDataViews(List<DataView> list) {
		dataviews = list;

		// new ArrayList<DataView>();
		// dataviews.add(new DataView("hits",
		// "application/x-clarin-fcs-hits+xml",
		// DeliveryPolicy.SEND_BY_DEFAULT));
		// dataviews.add(new DataView("kwic",
		// "application/x-clarin-fcs-kwic+xml",
		// DeliveryPolicy.NEED_TO_REQUEST));
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

	public void setSupportedLayers(List<Layer> list) {
		this.layers = list;
	}

	@Override
	public List<Layer> getSupportedLayers() {
		return layers;
	}

}
