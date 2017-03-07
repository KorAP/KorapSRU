package de.mannheim.ids.korap.test;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.http.client.HttpResponseException;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import de.mannheim.ids.korap.sru.KorapClient;
import de.mannheim.ids.korap.sru.KorapResult;
import de.mannheim.ids.korap.sru.QueryLanguage;


/**
 * The tests are based on the sample corpus from the Goethe corpus.
 * Skip the tests if Kustvakt does not have this corpus in the Krill
 * index.
 * The tests require a running KustvaktServer.
 * Specify the Kustvakt service URI in the configuration file at
 * src/main/resources/kustvakt.conf.
 * 
 * @author margaretha
 *
 */
public class KorapClientTest {
    private KorapClient c;


    public KorapClientTest () throws FileNotFoundException {
        c = new KorapClient(25, 50);
    }


    @Test
    public void testKorapClient () throws HttpResponseException, IOException {
        KorapResult result = c.query("Haus", QueryLanguage.CQL, "1.2", 1, 25,
                null);
        assertEquals(25, result.getMatches().size());

        result = c.query("(\"blaue\"|\"grüne\")", QueryLanguage.FCSQL, "2.0", 1,
                25, null);
        assertEquals(25, result.getMatches().size());
    }


    @Test
    public void testResource () throws HttpResponseException, Exception {
        JsonNode resources = c.retrieveResources();

        assertEquals(1, resources.size());
        assertEquals("Wikipedia", resources.get(0).get("name").asText());
    }
}
