package org.collectionspace.hello.test;

import org.junit.Assert;
import org.junit.Test;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @version $Revision: 1 $
 */
public class PersonServiceRawXmlTest {

    @Test
    public void testPersonResource() throws Exception {
        verbose("create a new Person");
        // Create a new object
        String newPerson = "<ns2:person xmlns:ns2=\"http://collectionspace.org/hello\">" + "<firstName>John</firstName>" + "<lastName>Doe</lastName>" + "<street>2195 Hearst Ave</street>" + "<city>Berkeley</city>" + "<state>CA</state>" + "<zip>94504</zip>" + "<country>USA</country>" + "</ns2:person>";

        URL postUrl = new URL("http://localhost:8080/helloworld/cspace/persons");
        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/xml");
        OutputStream os = connection.getOutputStream();
        os.write(newPerson.getBytes());
        os.flush();
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, connection.getResponseCode());
        String createdUrl = connection.getHeaderField("Location");
        verbose("Location: " + createdUrl);
        connection.disconnect();


        // Get the new object
        verbose("get created Person");
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

        String updatePerson = "<ns2:person xmlns:ns2=\"http://collectionspace.org/hello\">" + "<firstName>Jane</firstName>" + "<lastName>Doe</lastName>" + "<street>1 University Ave</street>" + "<city>Berkeley</city>" + "<state>CA</state>" + "<zip>94504</zip>" + "<country>USA</country>" + "</ns2:person>";
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/xml");
        os = connection.getOutputStream();
        os.write(updatePerson.getBytes());
        os.flush();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        connection.disconnect();

        // Show the update
        verbose("updated Person");
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setRequestMethod("GET");

        verbose("Content-Type: " + connection.getContentType());
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        line = reader.readLine();
        while (line != null) {
            verbose(line);
            line = reader.readLine();
        }
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        connection.disconnect();
    }

    private void verbose(String msg) {
        System.out.println("PersonServiceRawXmlTest : " + msg);
    }
}
