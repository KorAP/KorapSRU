package de.ids_mannheim.korap.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import de.ids_mannheim.korap.util.RedirectStrategy;

/**
 * The test requires a running KustvaktServer.
 * Specify the Kustvakt service URI at
 * /KorapSRU/src/main/webapp/WEB-INF/web.xml
 * 
 * @author margaretha
 *
 */
public class RedirectTest {

//    private String korapAPI = "http://localhost:8089/api/";
    private String korapAPI = "http://10.0.10.51:9000/api/";
    
    @Test
    public void testRedirect () throws ClientProtocolException, IOException {
        HttpClient client = HttpClientBuilder.create()
                .setRedirectStrategy(new RedirectStrategy()).build();
        HttpResponse response = client.execute(new HttpGet(
                korapAPI+"search?q=Wasser&ql=poliqarp"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        
        InputStream is = response.getEntity().getContent();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        String content = "";
        while ((line = br.readLine())!=null){
            content += line;
        }
        
        assertTrue(!content.isEmpty());
    }
}
