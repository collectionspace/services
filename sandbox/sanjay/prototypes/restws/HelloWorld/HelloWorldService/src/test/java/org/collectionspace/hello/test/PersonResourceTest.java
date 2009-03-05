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
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PersonResourceTest
{
   @Test
   public void testPersonResource() throws Exception
   {
      System.out.println("*** Create a new Person ***");
      // Create a new object
      String newPerson = "<person>"
              + "<firstName>John</firstName>"
              + "<lastName>Doe</lastName>"
              + "<street>2195 Hearst Ave</street>"
              + "<city>Berkeley</city>"
              + "<state>CA</state>"
              + "<zip>94504</zip>"
              + "<country>USA</country>"
              + "</person>";

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
      System.out.println("Location: " + createdUrl);
      connection.disconnect();


      // Get the new object
      System.out.println("*** GET Created Person **");
      URL getUrl = new URL(createdUrl);
      connection = (HttpURLConnection) getUrl.openConnection();
      connection.setRequestMethod("GET");
      System.out.println("Content-Type: " + connection.getContentType());

      BufferedReader reader = new BufferedReader(new
              InputStreamReader(connection.getInputStream()));

      String line = reader.readLine();
      while (line != null)
      {
         System.out.println(line);
         line = reader.readLine();
      }
      Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
      connection.disconnect();

      String updatePerson = "<person>"
              + "<firstName>Jane</firstName>"
              + "<lastName>Doe</lastName>"
              + "<street>1 University Ave</street>"
              + "<city>Berkeley</city>"
              + "<state>CA</state>"
              + "<zip>94504</zip>"
              + "<country>USA</country>"
              + "</person>";
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
      System.out.println("**** After Update ***");
      connection = (HttpURLConnection) getUrl.openConnection();
      connection.setRequestMethod("GET");

      System.out.println("Content-Type: " + connection.getContentType());
      reader = new BufferedReader(new
              InputStreamReader(connection.getInputStream()));

      line = reader.readLine();
      while (line != null)
      {
         System.out.println(line);
         line = reader.readLine();
      }
      Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
      connection.disconnect();
   }
}
