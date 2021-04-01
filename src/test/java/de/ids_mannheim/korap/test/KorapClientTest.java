package de.ids_mannheim.korap.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.HttpResponseException;
import org.junit.Test;

import de.ids_mannheim.korap.sru.KorapClient;
import de.ids_mannheim.korap.sru.KorapMatch;
import de.ids_mannheim.korap.sru.KorapResource;
import de.ids_mannheim.korap.sru.KorapResult;
import de.ids_mannheim.korap.sru.QueryLanguage;


/**
 * The tests are based on the sample corpus from the Goethe corpus.
 * 
 * @author margaretha
 *
 */
public class KorapClientTest {
    private KorapClient c;
    private KorapResult result;
    private KorapMatch match;
    
    public KorapClientTest () {
        c = new KorapClient("http://localhost:1080/api/v1.0", 25, 50);
    }

    @Test
    public void testCQLQuery () throws HttpResponseException, IOException {
        
        
        result = c.query("der", QueryLanguage.CQL, "1.2", 1, 1,
                null);
        assertEquals(1, result.getMatchSize());
        assertEquals(1858, result.getTotalResults());
    
        match = result.getMatch(0);
        assertEquals("match-GOE/AGA/01784-p18-19",match.getMatchId());
        
        match.parseMatchId();
        assertEquals("GOE", match.getCorpusId());
        assertEquals("AGA", match.getDocId());
        assertEquals("p18-19", match.getPositionId());
        
    }

    @Test
    public void testFCS2Query() throws HttpResponseException, IOException {
    	result = c.query("(\"blaue\"|\"grüne\")", QueryLanguage.FCSQL, "2.0", 1,
                25, null);
    	
    	assertEquals(25, result.getMatchSize());
        assertEquals(55, result.getTotalResults());

        match = result.getMatch(0);
        assertEquals("match-GOE/AGF/00000-p7744-7745",match.getMatchId());
        
        match.parseMatchId();
        assertEquals("GOE", match.getCorpusId());
        assertEquals("AGF", match.getDocId());
        assertEquals("p7744-7745", match.getPositionId());
	}
    
    @Test
    public void testRetrieveAnnotations() throws IOException, URISyntaxException {
    	String annotationSnippet = c.retrieveAnnotations(
    			"GOE", "AGF", "00000",
    			"p7667-7668", "*");
    	
    	assertEquals(true, annotationSnippet.startsWith("<snippet><span class="
    			+ "\"context-left\"></span><span class=\"match\"><span title="));
	}
    
    @Test
    public void testRetrieveNonexistingAnnotation() throws IOException, URISyntaxException {
    	String annotationSnippet = c.retrieveAnnotations(
    			"WPD15", "D18", "06488",
    			"p588-589", "*");
    	
    	assertEquals("<snippet></snippet>", annotationSnippet);
	}
    
    @Test
    public void testRetrieveResource () throws HttpResponseException, Exception {
        KorapResource[] resources = c.retrieveResources();
        assertEquals(3, resources.length);
        assertEquals("WPD17", resources[0].getResourceId());
        assertEquals("WDD17", resources[1].getResourceId());
        assertEquals("WUD17", resources[2].getResourceId());
    }
}
