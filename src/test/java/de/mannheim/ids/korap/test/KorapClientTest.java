package de.mannheim.ids.korap.test;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.HttpResponseException;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import de.mannheim.ids.korap.sru.KorapClient;
import de.mannheim.ids.korap.sru.KorapMatch;
import de.mannheim.ids.korap.sru.KorapResult;
import de.mannheim.ids.korap.sru.QueryLanguage;


/**
 * The tests are based on the sample corpus from the Goethe corpus.
 * Skip the tests if Kustvakt does not have this corpus in the Krill
 * index.
 * 
 * The tests require a running KustvaktServer.
 * Specify the Kustvakt service URI in the configuration file at
 * src/main/resources/kustvakt.conf.
 * 
 * @author margaretha
 *
 */
public class KorapClientTest {
    private KorapClient c;
    private KorapResult result;
    private KorapMatch match;

    public KorapClientTest () throws FileNotFoundException {
        c = new KorapClient(25, 50);
    }


    @Test
    public void testCQLQuery () throws HttpResponseException, IOException {
        result = c.query("Haus", QueryLanguage.CQL, "1.2", 1, 25,
                null);
        assertEquals(25, result.getMatchSize());
        assertEquals(136, result.getTotalResults());
    
        match = result.getMatch(0);
        assertEquals("match-GOE/AGF/00000-p8891-8892",match.getMatchId());
        
        match.parseMatchId();
        assertEquals("GOE", match.getCorpusId());
        assertEquals("AGF", match.getDocId());
        assertEquals("p8891-8892", match.getPositionId());
        
    }

    @Test
    public void testFCS2Query() throws HttpResponseException, IOException {
    	result = c.query("(\"blaue\"|\"grüne\")", QueryLanguage.FCSQL, "2.0", 1,
                25, null);
    	
    	assertEquals(25, result.getMatchSize());
        assertEquals(96, result.getTotalResults());

        match = result.getMatch(0);
        assertEquals("match-GOE/AGF/00000-p7667-7668",match.getMatchId());
        
        match.parseMatchId();
        assertEquals("GOE", match.getCorpusId());
        assertEquals("AGF", match.getDocId());
        assertEquals("p7667-7668", match.getPositionId());
	}
    
    @Test
    public void testRetrieveAnnotations() throws IOException, URISyntaxException {
    	String annotationSnippet = KorapClient.retrieveAnnotations(
    			"GOE", "AGF", "00000",
    			"p7667-7668", "*");
    	
    	assertEquals(true, annotationSnippet.startsWith("<snippet><span class="
    			+ "\"context-left\"></span><span class=\"match\"><span title="));
	}
    
    @Test
    public void testRetrieveNonexistingAnnotation() throws IOException, URISyntaxException {
    	String annotationSnippet = KorapClient.retrieveAnnotations(
    			"WPD15", "D18", "06488",
    			"p588-589", "*");
    	
    	assertEquals("<snippet></snippet>", annotationSnippet);
	}
    
    @Test
    public void testRetrieveResource () throws HttpResponseException, Exception {
        JsonNode resources = c.retrieveResources();

        //assertEquals(1, resources.size());
        assertEquals("Wikipedia", resources.get(0).get("name").asText());
        assertEquals("Deutsche Wikipedia 2015", resources.get(0).get("description").asText());
    }
}
