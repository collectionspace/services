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
public class CollectionObjectServiceRawXmlTest {

    @Test
    public void testCollectionObjectResource() throws Exception {
        verbose("create a new CollectionObject");
        // Create a new object
        String newCollectionObject = 
          "<collectionObject xmlns=\"http://collectionspace.org/hello\">" +
            "<serviceMetadata />" +
            "<defaultCollectionObject>" +
              "<objectNumber>1984.021.0049</objectNumber>" +
              "<objectName>Radio News, vol. 10, no. 2, August 1928</objectName>" +
            "</defaultCollectionObject>" +
          "</collectionObject>";
        verbose("new object: " + newCollectionObject);
        URL postUrl = new URL("http://localhost:8080/helloworld/cspace/collectionobjects");
        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/xml");
        OutputStream os = connection.getOutputStream();
        os.write(newCollectionObject.getBytes());
        os.flush();
        verbose("response: " + connection.getResponseMessage());
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, connection.getResponseCode());
        String createdUrl = connection.getHeaderField("Location");
        verbose("Location: " + createdUrl);
        connection.disconnect();


        // Get the new object
        verbose("get created CollectionObject");
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

        String updateCollectionObject = 
          "<collectionObject xmlns=\"http://collectionspace.org/hello\">" +
            "<serviceMetadata />" +
            "<defaultCollectionObject>" +
              "<objectNumber>1997.005.0437</objectNumber>" +
              "<objectName>Toy, Gotham City Police Helicopter, 1992</objectName>" +
            "</defaultCollectionObject>" +
          "</collectionObject>";
    
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/xml");
        os = connection.getOutputStream();
        os.write(updateCollectionObject.getBytes());
        os.flush();
        verbose("response: " + connection.getResponseMessage());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        connection.disconnect();

        // Show the update
        verbose("updated CollectionObject");
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
        System.out.println("CollectionObjectServiceRawXmlTest : " + msg);
    }
}
