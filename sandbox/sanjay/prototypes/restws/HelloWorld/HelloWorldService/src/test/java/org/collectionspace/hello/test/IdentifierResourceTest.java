package org.collectionspace.hello.test;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @version $Revision: 1 $
 */
public class IdentifierResourceTest {

    @Test
    public void testIdentifierResource() throws Exception {
        System.out.println("*** Create a new Identifier ***");
        // Create a new object
        String newIdentifier = "<ns2:identifier xmlns:ns2=\"http://collectionspace.org/hello\">" +
                "<namespace>edu.stanford</namespace>" +
                "</ns2:identifier>";

        URL postUrl = new URL("http://localhost:8080/helloworld/cspace/identifiers");
        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/xml");
        OutputStream os = connection.getOutputStream();
        os.write(newIdentifier.getBytes());
        os.flush();
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, connection.getResponseCode());
        String createdUrl = connection.getHeaderField("Location");
        System.out.println("Location: " + createdUrl);
        connection.disconnect();


        // Get the new object
        System.out.println("*** GET Created Identifier **");
        URL getUrl = new URL(createdUrl);
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setRequestMethod("GET");
        System.out.println("Content-Type: " + connection.getContentType());

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        connection.disconnect();

        connection.disconnect();
    }
}
