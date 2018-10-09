package de.ids_mannheim.korap.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import de.ids_mannheim.korap.util.RedirectStrategy;

public class RedirectTest {

    @Test
    public void testRedirect () throws ClientProtocolException, IOException {
        HttpClient client = HttpClientBuilder.create()
                .setRedirectStrategy(new RedirectStrategy()).build();
        HttpResponse response = client.execute(new HttpGet(
                "http://localhost:8089/api/search?q=Wasser&ql=poliqarp"));
        assertEquals(200, response.getStatusLine().getStatusCode());

    }
}
