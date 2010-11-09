/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.collectionspace.services.IntegrationTests.xmlreplay;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.IntegrationTests.xmlreplay.ServiceResult;

/**
 *   @author Laramie Crocker
 */
public class XmlReplayTransport {

    private static String BOUNDARY = "34d97c83-0d61-4958-80ab-6bf8d362290f";
        private static String DD = "--";
        private static String CRLF = "\r\n";

    public static ServiceResult doGET(String urlString, String authForTest, String fromTestID) throws Exception {
        HttpClient client = new HttpClient();
        GetMethod getMethod = new GetMethod(urlString);
        getMethod.addRequestHeader("Accept", "multipart/mixed");
        getMethod.addRequestHeader("Accept", "application/xml");
        getMethod.setRequestHeader("Authorization", "Basic " + authForTest); //"dGVzdDp0ZXN0");
        getMethod.setRequestHeader("X-XmlReplay-fromTestID", fromTestID);
        ServiceResult pr = new ServiceResult();

        int statusCode1 = client.executeMethod(getMethod);
        pr.responseCode = statusCode1;
        pr.method = "GET";
        try {
            pr.result = getMethod.getResponseBodyAsString();
            pr.responseMessage = getMethod.getStatusText();
        } catch (Throwable t){
            //System.err.println("ERROR getting content from response: "+t);
            pr.error = t.toString();
        }


        getMethod.releaseConnection();
        return pr;
    }

    public static ServiceResult doDELETE(String urlString, String authForTest, String testID, String fromTestID) throws Exception {
        ServiceResult pr = new ServiceResult();
        pr.method = "DELETE";
        pr.fullURL = urlString;
        if (Tools.isEmpty(urlString)){
            pr.error = "url was empty.  Check the result for fromTestID: "+fromTestID+". currentTest: "+testID;
            return pr;
        }
        HttpClient client = new HttpClient();
        DeleteMethod deleteMethod = new DeleteMethod(urlString);
        deleteMethod.setRequestHeader("Accept", "multipart/mixed");
        deleteMethod.addRequestHeader("Accept", "application/xml");
        deleteMethod.setRequestHeader("Authorization", "Basic " + authForTest);
        deleteMethod.setRequestHeader("X-XmlReplay-fromTestID", fromTestID);
        int statusCode1 = 0;
        String res = "";
        try {
            statusCode1 = client.executeMethod(deleteMethod);
            pr.responseCode = statusCode1;
            //System.out.println("statusCode: "+statusCode1+" statusLine ==>" + deleteMethod.getStatusLine());
            pr.responseMessage = deleteMethod.getStatusText();
            res = deleteMethod.getResponseBodyAsString();
            deleteMethod.releaseConnection();
        } catch (Throwable t){
            pr.error = t.toString();
        }
        pr.result = res;
        pr.responseCode = statusCode1;
        return pr;
    }

    public static ServiceResult doLIST(String urlString, String listQueryParams, String authForTest, String fromTestID) throws Exception {
        //String u = Tools.glue(urlString, "/", "items/");
        if (Tools.notEmpty(listQueryParams)){
            urlString = Tools.glue(urlString, "?", listQueryParams);
        }
        return doGET(urlString, authForTest, fromTestID);
    }

    public static final String MULTIPART_MIXED = "multipart/mixed";
    public static final String APPLICATION_XML = "application/xml";

    /** Use this overload for multipart messages. */
    public static ServiceResult doPOST_PUTFromXML_Multipart(List<String> filesList,
                                                                      List<String> partsList,
                                                                      String protoHostPort,
                                                                      String uri,
                                                                      String method,
                                                                      XmlReplayEval evalStruct,
                                                                      String authForTest,
                                                                      String fromTestID)
                                                                      throws Exception {
        if (  filesList==null||filesList.size()==0
            ||partsList==null||partsList.size()==0
            ||(partsList.size() != filesList.size())){
            throw new Exception("filesList and partsList must not be empty and must have the same number of items each.");
        }
        String content = DD + BOUNDARY;

        for (int i=0; i<partsList.size(); i++){
            String fileName = filesList.get(i);
            String commonPartName = partsList.get(i);
            byte[] b = FileUtils.readFileToByteArray(new File(fileName));
            String xmlString = new String(b);

            xmlString = evalStruct.eval(xmlString, evalStruct.serviceResultsMap, evalStruct.jexl, evalStruct.jc);

            content = content + CRLF + "label: "+commonPartName + CRLF
                              + "Content-Type: application/xml" + CRLF
                              + CRLF
                              + xmlString + CRLF
                              + DD + BOUNDARY;
        }
        content = content + DD;
        String urlString = protoHostPort+uri;
        return doPOST_PUT(urlString, content, BOUNDARY, method, MULTIPART_MIXED, authForTest, fromTestID); //method is POST or PUT.
    }

    /** Use this overload for NON-multipart messages, that is, regular POSTs. */
        public static ServiceResult doPOST_PUTFromXML(String fileName,
                                                                String protoHostPort,
                                                                String uri,
                                                                String method,
                                                                String contentType,
                                                                XmlReplayEval evalStruct,
                                                                String authForTest,
                                                                String fromTestID)
    throws Exception {
        byte[] b = FileUtils.readFileToByteArray(new File(fileName));
        String xmlString = new String(b);
        xmlString = evalStruct.eval(xmlString, evalStruct.serviceResultsMap, evalStruct.jexl, evalStruct.jc);
        String urlString = protoHostPort+uri;
        return doPOST_PUT(urlString, xmlString, BOUNDARY, method, contentType, authForTest, fromTestID); //method is POST or PUT.
    }


    public static ServiceResult doPOST_PUT(String urlString, String content, String boundary, String method, String contentType,
                                           String authForTest, String fromTestID) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn;
        conn = (HttpURLConnection) url.openConnection();

        if (MULTIPART_MIXED.equalsIgnoreCase(contentType)){
            conn.setRequestProperty("Accept", "multipart/mixed");
            conn.setRequestProperty("content-type", "multipart/mixed; boundary=" + boundary);
        } else {
            conn.setRequestProperty("Accept", "application/xml");
            conn.setRequestProperty("content-type", contentType);
        }
        conn.setRequestProperty("Authorization", "Basic " + authForTest);  //TODO: remove test user : hard-coded as "dGVzdDp0ZXN0"
        conn.setRequestProperty("Connection", "close");
        conn.setRequestProperty("X-XmlReplay-fromTestID", fromTestID);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod(method); // "POST" or "PUT"
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(content);
        wr.flush();

        ServiceResult result = new ServiceResult();
        try {
            result.responseCode = conn.getResponseCode();
            //System.out.println("responseCode: "+result.responseCode);
            if (400 <= result.responseCode && result.responseCode <= 499){
                return result;
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                sb.append(line).append("\r\n");
            }
            String msg = sb.toString();
            result.result = msg;
            rd.close();
        } catch (Throwable t){
            //System.err.println("ERROR getting content from response: "+t);
            result.error = t.toString();
        }
        wr.close();


        String deleteURL = "";
        String location = "";
        Map<String, List<String>> headers = conn.getHeaderFields();
        List<String> locations = headers.get("Location");
        if (locations != null){
            String locationZero = locations.get(0);
            if (locationZero != null){
                String[] segments = locationZero.split("/");
                location = segments[segments.length - 1];
                deleteURL = Tools.glue(urlString, "/", location);
            }
        }
        result.location = location;
        result.deleteURL = deleteURL;
        result.CSID = location;
        result.method = method;
        return result;
    }

}
