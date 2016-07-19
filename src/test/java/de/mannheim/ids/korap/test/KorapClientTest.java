package de.mannheim.ids.korap.test;

import org.apache.http.client.HttpResponseException;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import de.mannheim.ids.korap.sru.KorapClient;
import de.mannheim.ids.korap.sru.KorapResult;
import de.mannheim.ids.korap.sru.QueryLanguage;


public class KorapClientTest {

	@Test
	public void testKorapClient() throws Exception{
		KorapClient c = new KorapClient(25,50);
		
//		prox Kuh
		KorapResult result = c.query("Haus", QueryLanguage.CQL, "1.2", 1, 5,
				null);
		//System.out.println(result.getMatches().size());
		
	}
	
	@Test
	public void testResource() throws HttpResponseException, Exception {
		KorapClient c = new KorapClient(25,50);
		JsonNode resources = c.retrieveResources();
		
		for (JsonNode r : resources){
			System.out.println(r);
			System.out.println(r.get("name"));
		}

	}
	
}
