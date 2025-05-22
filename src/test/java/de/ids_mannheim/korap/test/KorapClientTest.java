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
import eu.clarin.sru.server.SRUException;

/**
 * The tests are based on the sample corpus from the Goethe corpus.
 * 
 * @author margaretha
 *
 */
public class KorapClientTest extends BaseTest {
    private KorapClient c;
    private KorapResult result;
    private KorapMatch match;

    public KorapClientTest () {
        c = new KorapClient("http://localhost:1080", 25, 50);
    }

	@Test
	public void testCQLQuery ()
			throws HttpResponseException, IOException, SRUException {

		   createExpectationForSearch("der", "cql", "1.2", "50",
                "search-der.jsonld");

        result = c.query("der", QueryLanguage.CQL, "1.2", 51, 1, null);
        assertEquals(1, result.getMatchSize());
        assertEquals(1858, result.getTotalResults());

        match = result.getMatch(0);
        assertEquals("match-GOE/AGA/01784-p1856-1857", match.getMatchId());

        match.parseMatchId();
        assertEquals("GOE", match.getCorpusId());
        assertEquals("AGA", match.getDocId());
        assertEquals("p1856-1857", match.getPositionId());

    }

	@Test
	public void testOrQuery ()
			throws HttpResponseException, IOException, SRUException {

        createExpectationForSearch("(\"blaue\"|\"grüne\")", "fcsql", "2.0", "0",
                "search-or.jsonld");

        createExpectationForMatchInfo("GOE-AGF-00000-p7744-7745.jsonld",
                "/corpus/GOE/AGF/00000/p7744-7745/matchInfo");

        result = c.query("(\"blaue\"|\"grüne\")", QueryLanguage.FCSQL, "2.0", 1,
                1, null);

        assertEquals(1, result.getMatchSize());
        assertEquals(55, result.getTotalResults());

        match = result.getMatch(0);
        assertEquals("match-GOE/AGF/00000-p7744-7745", match.getMatchId());

        match.parseMatchId();
        assertEquals("GOE", match.getCorpusId());
        assertEquals("AGF", match.getDocId());
        assertEquals("p7744-7745", match.getPositionId());
    }

    @Test
    public void testRetrieveAnnotations ()
            throws IOException, URISyntaxException {
        createExpectationForMatchInfo("GOE-AGF-00000-p4276-4277.jsonld",
                "/corpus/GOE/AGF/00000/p4276-4277/matchInfo");

        String annotationSnippet =
                c.retrieveAnnotations("GOE", "AGF", "00000", "p4276-4277", "*");

        assertEquals(
                "<snippet><span class=\"context-left\"><span class=\"more\">"
                        + "</span></span><span class=\"match\"><mark>feineren</mark>"
                        + "</span><span class=\"context-right\"><span class=\"more\">"
                        + "</span></span></snippet>",
                annotationSnippet);
    }

    @Test
    public void testRetrieveNonexistingAnnotation ()
            throws IOException, URISyntaxException {

        createExpectationForMatchInfo("unknownMatchInfo.jsonld",
                "/corpus/WPD15/D18/06488/p588-589/matchInfo");

        String annotationSnippet =
                c.retrieveAnnotations("WPD15", "D18", "06488", "p588-589", "*");

        assertEquals("<snippet></snippet>", annotationSnippet);
    }

    @Test
    public void testRetrieveResource ()
            throws HttpResponseException, Exception {
        createExpectationForRetrieveResource();
        KorapResource[] resources = c.retrieveResources();
        assertEquals(3, resources.length);
		assertEquals("http://hdl.handle.net/10932/00-03B6-558F-4E10-6201-1",
				resources[0].getResourceId());
        assertEquals("http://hdl.handle.net/10932/00-03B6-558F-5EA0-6301-B", 
        		resources[1].getResourceId());
        assertEquals("http://hdl.handle.net/10932/00-03B6-558F-6EF0-6401-F", 
        		resources[2].getResourceId());
    }
}
