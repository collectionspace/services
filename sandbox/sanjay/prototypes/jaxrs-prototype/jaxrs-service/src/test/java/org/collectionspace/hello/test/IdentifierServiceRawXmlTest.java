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
public class IdentifierServiceRawXmlTest {

    @Test
    public void testIdentifierResource() throws Exception {
        verbose("create a new Identifier");
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
        verbose("Location: " + createdUrl);
        connection.disconnect();


        // Get the new object
        verbose("get created Identifier");
        URL getUrl = new URL(createdUrl);
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setRequestMethod("GET");
        verbose("Content-Type: " + connection.getContentType());

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String line = reader.readLine();
        while (line != null) {
            verbose(line);
            line = reader.readLine();
        }
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        connection.disconnect();

    }

    private void verbose(String msg) {
        System.out.println("IdentifierServiceRawXmlTest : " + msg);
    }
}
