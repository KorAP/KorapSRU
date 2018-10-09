package de.ids_mannheim.korap.test;

import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;

import eu.clarin.sru.server.utils.SRUServerServlet;

public class KorapJerseyTest extends JerseyTest {

    @Override
    protected AppDescriptor configure () {
        return new WebAppDescriptor.Builder()
                .servletClass(SRUServerServlet.class)
                .initParam("eu.clarin.sru.server.utils.sruServerConfigLocation",
                        "/src/main/webapp/WEB-INF/sru-server-config.xml")
                .initParam(
                        "eu.clarin.sru.server.utils.sruServerSearchEngineClass",
                        "de.ids_mannheim.korap.sru.KorapSRU")
                .initParam("eu.clarin.sru.server.database", "korapsru")
                .initParam("eu.clarin.sru.server.sruSupportedVersionMax", "2.0")
                .initParam("eu.clarin.sru.server.sruSupportedVersionDefault", "2.0")
                .initParam("eu.clarin.sru.server.numberOfRecords", "25")
                .initParam("eu.clarin.sru.server.maximumRecords", "50")
                .contextParam("de.ids_mannheim.korap.endpointDescription",
                        "/src/main/webapp/WEB-INF/endpoint-description.xml")
                .contextParam("korap.service.uri", "http://localhost:8089/api/v1.0/")
                .build();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory ()
            throws TestContainerException {
        return new GrizzlyWebTestContainerFactory();
    }

    @Override
    protected int getPort (int defaultPort) {
        return 8080;
    }
}
