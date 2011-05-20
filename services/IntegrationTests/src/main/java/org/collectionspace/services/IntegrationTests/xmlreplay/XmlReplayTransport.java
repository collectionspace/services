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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.common.api.Tools;

/**
 *   @author Laramie Crocker
 */
public class XmlReplayTransport {

    private static String BOUNDARY = "34d97c83-0d61-4958-80ab-6bf8d362290f";
        private static String DD = "--";
        private static String CRLF = "\r\n";

    public static ServiceResult doGET(String urlString, String authForTest, String fromTestID) throws Exception {
        ServiceResult pr = new ServiceResult();
        pr.fromTestID = fromTestID;
        pr.method = "GET";
        //HACK for speed testing.
        //pr.CSID = "2";
        //pr.overrideGotExpectedResult();
        //if (true) return pr;
        //END-HACK
        HttpClient client = new HttpClient();
        GetMethod getMethod = new GetMethod(urlString);
        getMethod.addRequestHeader("Accept", "multipart/mixed");
        getMethod.addRequestHeader("Accept", "application/xml");
        getMethod.setRequestHeader("Authorization", "Basic " + authForTest); //"dGVzdDp0ZXN0");
        getMethod.setRequestHeader("X-XmlReplay-fromTestID", fromTestID);
        try {
            int statusCode1 = client.executeMethod(getMethod);
            pr.responseCode = statusCode1;
            pr.result = getMethod.getResponseBodyAsString();
            pr.responseMessage = getMethod.getStatusText();
            Header[] headers = getMethod.getResponseHeaders();
            pr.responseHeaders = Arrays.copyOf(headers, headers.length);
            Header hdr = getMethod.getResponseHeader("CONTENT-TYPE");
            if (hdr!=null){
                String hdrStr = hdr.toExternalForm();
                pr.boundary = PayloadLogger.parseBoundary(hdrStr);
            }
            pr.contentLength = getMethod.getResponseContentLength();
            getMethod.releaseConnection();
        } catch (Throwable t){
            //System.err.println("ERROR getting content from response: "+t);
            pr.error = t.toString();
        }
        return pr;
    }

    public static ServiceResult doDELETE(String urlString, String authForTest, String testID, String fromTestID) throws Exception {
        ServiceResult pr = new ServiceResult();
        pr.failureReason = "";
        pr.method = "DELETE";
        pr.fullURL = urlString;
        pr.fromTestID = fromTestID;
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
    /**
    public static ServiceResult doPOST_PUTFromXML_Multipart(List<String> filesList,
                                                            List<String> partsList,
                                                            List<Map<String,String>> varsList,
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
        Map<String, String> contentRaw = new HashMap<String, String>();
        for (int i=0; i<partsList.size(); i++){
            String fileName = filesList.get(i);
            String commonPartName = partsList.get(i);
            byte[] b = FileUtils.readFileToByteArray(new File(fileName));
            String xmlString = new String(b);

            xmlString = evalStruct.eval(xmlString, evalStruct.serviceResultsMap, varsList.get(i), evalStruct.jexl, evalStruct.jc);
            contentRaw.put(commonPartName, xmlString);
            content = content + CRLF + "label: "+commonPartName + CRLF
                              + "Content-Type: application/xml" + CRLF
                              + CRLF
                              + xmlString + CRLF
                              + DD + BOUNDARY;
        }
        content = content + DD;
        String urlString = protoHostPort+uri;
        return doPOST_PUT(urlString, content, contentRaw, BOUNDARY, method, MULTIPART_MIXED, authForTest, fromTestID); //method is POST or PUT.
    }
    */

    /** Use this overload for NON-multipart messages, that is, regular POSTs. */
    public static ServiceResult doPOST_PUTFromXML(String fileName,
                                                      Map<String,String> vars,
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
        String contentRaw = xmlString;
        xmlString = evalStruct.eval(xmlString, evalStruct.serviceResultsMap, vars, evalStruct.jexl, evalStruct.jc);
        String urlString = protoHostPort+uri;
        return doPOST_PUT(urlString, xmlString, contentRaw, BOUNDARY, method, contentType, authForTest, fromTestID); //method is POST or PUT.
    }

        //HACK for speed testing in doPOST_PUT.
        //  Result: XmlReplay takes 9ms to process one test
        // right up to the point of actually firing an HTTP request.
        // or ~ 120 records per second.
        //result.CSID = "2";
        //result.overrideGotExpectedResult();
        //if (true) return result;
        //END-HACK

    public static ServiceResult doPOST_PUT(String urlString,
                                                                     String content,
                                                                     String contentRaw,
                                                                     String boundary,
                                                                     String method,
                                                                     String contentType,
                                                                     String authForTest,
                                                                     String fromTestID) throws Exception {
        ServiceResult result = new ServiceResult();
        result.method = method;
        String deleteURL = "";
        String location = "";
        try {
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

            try {
                result.requestPayload = content;
                result.requestPayloadsRaw = contentRaw;
                result.responseCode = conn.getResponseCode();
                //System.out.println("responseCode: "+result.responseCode);
                if (400 <= result.responseCode && result.responseCode <= 499){
                    return result;
                }
                readStream(conn, result);
            } catch (Throwable t){
                //System.err.println("ERROR getting content from response: "+t);
                result.error = t.toString();
            }
            wr.close();

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
        } catch (Throwable t2){
            result.error = "ERROR in XmlReplayTransport: "+t2;
        }
        return result;
    }

    public static ServiceResult doPOST_PUT_PostMethod(String urlString, String content, Map<String,String> contentRaw,
                                           String boundary, String method, String contentType,
                                           String authForTest, String fromTestID) throws Exception {
        ServiceResult result = new ServiceResult();
        result.method = method;
        String deleteURL = "";
        String location = "";
        try {
            HttpClient client = new HttpClient();
            PostMethod postMethod = new PostMethod(urlString);
            postMethod.setRequestHeader("Accept", "multipart/mixed");
            postMethod.addRequestHeader("Accept", "application/xml");
            postMethod.setRequestHeader("Authorization", "Basic " + authForTest);
            postMethod.setRequestHeader("X-XmlReplay-fromTestID", fromTestID);
            //this method takes an array of params.  Not sure what they expect us to do with a raw post:
            //   postMethod.setRequestBody();
            int statusCode1 = 0;
            String res = "";
            try {
                statusCode1 = client.executeMethod(postMethod);
                result.responseCode = statusCode1;
                //System.out.println("statusCode: "+statusCode1+" statusLine ==>" + postMethod.getStatusLine());
                result.responseMessage = postMethod.getStatusText();
                res = postMethod.getResponseBodyAsString();
                Header[] headers = postMethod.getResponseHeaders("Location");
                if (headers.length>0) {
                    System.out.println("headers[0]:  "+headers[0]);
                    String locationZero = headers[0].getValue();
                    if (locationZero != null){
                        String[] segments = locationZero.split("/");
                        location = segments[segments.length - 1];
                        deleteURL = Tools.glue(urlString, "/", location);
                    }
                }
                postMethod.releaseConnection();
            } catch (Throwable t){
                result.error = t.toString();
            }
            result.result = res;
            result.location = location;
            result.deleteURL = deleteURL;
            result.CSID = location;
        } catch (Throwable t2){
            result.error = "ERROR in XmlReplayTransport: "+t2;
        }
        return result;
    }

    private static void readStream(HttpURLConnection  conn, ServiceResult result) throws Throwable {
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        try {
                String line;
                StringBuffer sb = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    sb.append(line).append("\r\n");
                }
                String msg = sb.toString();
                result.result = msg;
                result.boundary = PayloadLogger.parseBoundary(conn.getHeaderField("CONTENT-TYPE"));
        } finally {
            rd.close();
        }
    }

}
