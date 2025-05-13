package de.ids_mannheim.korap.sru;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import eu.clarin.sru.server.SRUConfigException;
import eu.clarin.sru.server.SRUConstants;
import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.fcs.DataView;
import eu.clarin.sru.server.fcs.DataView.DeliveryPolicy;
import eu.clarin.sru.server.fcs.EndpointDescription;
import eu.clarin.sru.server.fcs.Layer;
import eu.clarin.sru.server.fcs.ResourceInfo;
import eu.clarin.sru.server.fcs.ResourceInfo.AvailabilityRestriction;
import eu.clarin.sru.server.fcs.utils.SimpleEndpointDescriptionParser;

/**
 * Contains information for generating a response of SRU explain
 * operation with endpoint description.
 * 
 * Example:
 * http://localhost:8080/KorapSRU?operation=explain&x-fcs-endpoint
 * -description=true
 * 
 * @author margaretha
 * 
 */
public class KorapEndpointDescription implements EndpointDescription {

    private List<DataView> dataviews;
    private List<URI> capabilities;
    private List<String> languages;

    private List<String> defaultDataviews;
    private List<Layer> layers;
    private Layer textLayer;

    private List<AnnotationLayer> annotationLayers;
    
    public KorapEndpointDescription () {}
    
    public KorapEndpointDescription (ServletContext context)
            throws SRUConfigException {
        
        String endpointDesc = context.getInitParameter("de.ids_mannheim.korap.endpointDescription");
        if (endpointDesc==null || endpointDesc.isEmpty()){
            endpointDesc = "/WEB-INF/endpoint-description.xml";
        }
        
        try {
            URL url = context.getResource(endpointDesc);
            EndpointDescription simpleEndpointDescription =
                    SimpleEndpointDescriptionParser.parse(url);
            if (simpleEndpointDescription != null) {
                setSupportedLayers(
                        simpleEndpointDescription.getSupportedLayers());
                setAnnotationLayers(
                        simpleEndpointDescription.getSupportedLayers());
                setSupportedDataViews(
                        simpleEndpointDescription.getSupportedDataViews());
                setDefaultDataViews(
                        simpleEndpointDescription.getSupportedDataViews());
                setCapabilities(simpleEndpointDescription.getCapabilities());
            }

        }
        catch (MalformedURLException e) {
            throw new SRUConfigException(
                    "error initializing resource info inventory", e);
        }
        setLanguages();
    }

    @Override
    public void destroy () {
        //dataviews.clear();
        //capabilities.clear();
        dataviews = null;
        capabilities = null;
        languages.clear();
    }

    public void setLanguages () {
        languages = new ArrayList<String>();
        languages.add("deu");
    }

    @Override
    public List<URI> getCapabilities () {
        return capabilities;
    }

    public void setCapabilities (List<URI> list) throws SRUConfigException {
        capabilities = list;
    }

    @Override
    public List<DataView> getSupportedDataViews () {
        return dataviews;
    }

    public void setSupportedDataViews (List<DataView> list) {
        dataviews = list;
    }

    @Override
    public List<ResourceInfo> getResourceList (String pid) throws SRUException {

        List<ResourceInfo> resourceList = new ArrayList<ResourceInfo>();

        Map<String, String> description;

        KorapResource[] resources;
        try {
            resources = KorapSRU.korapClient.retrieveResources();
        }
        catch (URISyntaxException | IOException e) {
            throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                    "Failed retrieving resources.");
        }

        for (KorapResource r : resources) {
            description = new HashMap<String, String>();
            description.put("en", r.getDescription());

			ResourceInfo ri = new ResourceInfo(r.getResourceId(), r.getTitles(),
					description, null, r.getLandingPage(),
					Arrays.asList(r.getLanguages()),
					AvailabilityRestriction.NONE, dataviews,
					this.getSupportedLayers(), null);
			resourceList.add(ri);
        }

        return resourceList;
    }

    public List<String> getDefaultDataViews () {
        return defaultDataviews;
    }

    public void setDefaultDataViews (List<DataView> supportedDataViews) {
        defaultDataviews = new ArrayList<String>();
        for (DataView d : supportedDataViews) {
            if (d.getDeliveryPolicy() == DeliveryPolicy.SEND_BY_DEFAULT) {
                defaultDataviews.add(d.getIdentifier());
            }
        }
    }

    public void setSupportedLayers (List<Layer> layers) {
        this.layers = layers;
    }

    @Override
    public List<Layer> getSupportedLayers () {
        return layers;
    }

    public List<AnnotationLayer> getAnnotationLayers () {
        return annotationLayers;
    }

    public void setAnnotationLayers (List<Layer> layers) {
        annotationLayers = new ArrayList<AnnotationLayer>(layers.size());

        String layerCode;

        for (Layer l : layers) {

            String type = l.getType();
            if (type.equals(AnnotationLayer.TYPE.TEXT.toString())) {
                layerCode = type;
                this.textLayer = l;
            }
            else {
                StringBuilder sb = new StringBuilder();
                String qualifier = l.getQualifier();

                if (qualifier != null) {
                    sb.append(qualifier);

                    if (type.equals(AnnotationLayer.TYPE.POS.toString())) {
                        sb.append("/p");
                    }
                    else if (type
                            .equals(AnnotationLayer.TYPE.LEMMA.toString())) {
                        sb.append("/l");
                    }
                    else {
                        continue;
                    }
                }
                layerCode = sb.toString();
            }

            AnnotationLayer annotationLayer =
                    new AnnotationLayer(layerCode, l.getResultId());
            annotationLayers.add(annotationLayer);
        }
    }

    public Layer getTextLayer () {
        return textLayer;
    }

    public void setTextLayer (Layer textLayer) {
        this.textLayer = textLayer;
    }

    @Override
    public int getVersion () {
        return VERSION_2;
    }

    @Override
    public boolean isVersion (int version) {
        if(version == VERSION_2) {
            return true;
        }
        else {
            return false;
        }
    }

	@Override
	public ResourceInfo getResource (String pid) throws SRUException {
		// TODO Auto-generated method stub
		return null;
	}
}
