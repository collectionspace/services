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

import org.collectionspace.services.common.api.Tools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PayloadLogger{

    public static void saveReport(){
        reporter.writeTable();
    }

    private static Reporter reporter = new Reporter();

    private static volatile int ID = 1000;
    public String getID(){
        return ""+ID;
    }

    private static String DD = "--";
    private static String LABEL="LABEL: ";

    static class Reporter {
        public Reporter(){
            table.append("<html><body><table border='1'>\r\n");
        }
        public static final String START = "<tr><td>";
        public static final String SEP = "</td><td>";
        public static final String END = "</td></tr>";

        public void writeTable(){
            table.append("</table></body></html>");
            saveFile("./xml/", "results.html", table.toString());
        }

        private StringBuffer table = new StringBuffer();

        public synchronized void finished(HttpTraffic traffic){
            String direction = traffic.isRequest ?
                                "<span style='background-color: lightgreen;'>==&gt;</span>"
                                :
                                " &lt;==";
            table.append(START)
                    .append(direction)
                    .append(SEP)
                    .append(traffic==null ? "null" : traffic.toRow(SEP, this))
                    .append(END);
        }

        public synchronized void finished(List<HttpTraffic> trafficList){
            for (HttpTraffic traffic: trafficList){
                finished(traffic);
            }
        }

        public static String link(String desc, String url){
            return "<a href='"+url+"'>"+desc+"</a>";
        }
        public static final String newline = "<br />\r\n";

    }


    static class HttpTraffic {
        public HttpTraffic(){
            payloads = new ArrayList<Part>();
        }
        public List<Part> payloads;
        public String method = "";
        public String url = "";
        public String queryParams = "";
        public String message = "";
        public int responseCode = 0;
        public String boundary = "";
        public String location = "";
        public long contentLength = -1;
        public boolean isRequest = true;
        public boolean extra = false;
        public String ID = "0";
        public int nexti = 0;

        public Part getPart(String label){
            for (Part part : payloads){
                if (part.label.equalsIgnoreCase(label)){
                    return part;
                }
            }
            return null;
        }

        public String toRow(String sep, Reporter reporter){
            StringBuffer b = new StringBuffer();
            for (Part part : payloads){
                String name = part.label;
                if (part.filename.length()==0){
                    continue;
                }
                if (name.trim().length()<=0){
                    name = ID;
                }
                name = name+" ("+part.filetype+')';
                String link = reporter.link(name, part.filename);
                b.append(link)
                 .append(reporter.newline);
            }
            String parts = b.toString();
            if (isRequest){
                return  ID+sep
                        +method+sep
                        +url+sep
                        +queryParams+sep
                        +sep
                        +parts;
            } else {
                return  ID+sep
                        +responseCode+sep
                        +message+sep
                        +location+sep
                        +contentLength+sep
                        +url
                        +parts;
            }
        }
    }

    static class Part {
        public Part(String boundary){
            this.boundary = boundary;
        }
        public boolean isMultipart(){
            return boundary.length()>0;
        }
        public String toString(){
            return "Part:"+label+";";
        }
        public String filename = "";
        public String filetype = "";
        public String boundary;
        public StringBuffer buffer = new StringBuffer();
        public String getContent(){
            return buffer.toString();
        }
        public String label = "";
        public int readPart(String[]lines, int i){
            String line = "";
            boolean readingPartHeaders = true;
            while(readingPartHeaders){
                line = killTrailingWS(lines[i]);
                if (line.toUpperCase().startsWith(LABEL)){
                    this.label = line.substring(LABEL.length()).trim();
                } else if (line.trim().length()==0){
                    readingPartHeaders = false;
                }
                i++;
            }
            while (i<lines.length){
                line = lines[i];
                if (line.startsWith(DD+boundary)){
                    return i;   
                }
                this.buffer.append(line).append("\r\n");   //todo: maybe don't add CRLF on last line.
                i++;
            }
            return i;               
        }
        public int readRemaining(String [] lines, int i, long contentLength){
            String line;
            int bytesRead=0;
            while (i<lines.length && bytesRead<contentLength){
                line = killTrailingWS(lines[i]);
                if (line.startsWith("HTTP/1.1")){
                    return i;
                }
                int read = line.length();
                bytesRead += read;
                buffer.append(line).append("\r\n");   //todo: maybe don't add CRLF on last line.
                i++;
            }
            return i;
        }
    }

    public static String parseBoundary(String headerLine) {
        if (Tools.isEmpty(headerLine)) {
            return "";
        }
        String lineUP = headerLine.toUpperCase();
        String boundary = "";
        if (lineUP.startsWith("CONTENT-TYPE:")) {
            String[] boundaryTokens = headerLine.split("boundary=");
            if (boundaryTokens.length == 2) {
                boundary = killTrailingWS(boundaryTokens[1]);
                //Header might be:
                // Content-Type: multipart/mixed; boundary=a97c20ab-3ef6-4adc-82b0-6cf28c450faf;charset=ISO-8859-1

                String[] boundaryTerm = boundary.split(";");
                boundary = boundaryTerm[0];

            } else if (boundaryTokens.length > 2) {
                System.err.println("WARNING: too many tokens after boundary= on Content-Type: header line: " + headerLine);
            }
        }
        return boundary;
    }

    /** places the boundary on the HttpTraffic in parameter object if boundary found in header "Content-Type:".
     *  @return the index of the NEXT line the caller should read. */
    protected static int readHeaders(HttpTraffic traffic, String[]lines, int i){
        int lineCount = lines.length;
        String line, lineUP;
        // Now read headers until we are ready for payload or parts.
        while (i<lineCount){
            line = lines[i];
            if (line.trim().length()==0){  //blank line seen: end of headers.
                i++;
                break;
            } else {  //still reading outer headers.
                lineUP = line.toUpperCase().trim();
                if (lineUP.startsWith("CONTENT-TYPE:")){
                    String[] boundaryTokens = line.split("boundary=");
                    if (boundaryTokens.length == 2){
                        traffic.boundary = killTrailingWS(boundaryTokens[1]);

                    } else if (boundaryTokens.length > 2){
                        System.err.println("WARNING: too many tokens after boundary= on Content-Type: header line: "+line);
                    }
                } else if (lineUP.startsWith("LOCATION: ")){
                    traffic.location = killTrailingWS(line.substring("LOCATION: ".length()));
                } else if (lineUP.startsWith("CONTENT-LENGTH: ")){
                    traffic.contentLength = Integer.parseInt(killTrailingWS(line.substring("CONTENT-LENGTH: ".length())));
                }
                i++;
            }
        }
        return i;
    }


    //  0  1  2  3
    //  a  b  c  \r

    private static String killTrailingWS(String s){
        int i = s.length();
        while (i>0){
            char c = s.charAt(i-1);
            if (c=='\r' || c=='\n' || c==' '){
                i--;
                continue;
            } else {
                break;
            }
        }
        return s.substring(0, i);
    }

    public static HttpTraffic readPayloads(String fullPayload, String boundary, long contentLength){
        HttpTraffic traffic = new HttpTraffic();
        traffic.contentLength = contentLength;
        traffic.boundary = boundary;
        String [] lines = fullPayload.split("\\n", -1);
        readPayloads(traffic, lines, 0);
        return traffic;
    }

    protected static int readPayloads(HttpTraffic traffic, String[]lines, int i){
        if (traffic.boundary.length()<=0){   //END of headers, and no boundary, so read remaining and return.
            if (traffic.contentLength == 0){
                return i;
            }
            Part part = new Part("");
            traffic.payloads.add(part);
            i = part.readRemaining(lines, i, traffic.contentLength);
            return i;
        }
        int lineCount = lines.length;
        String line;
        while (i<lineCount){
            //rest of message is payloads.
            line = lines[i];

            if (line.startsWith( DD + traffic.boundary + DD )){   //this is the ending boundary.
                //close and accept payload chunk.
                i++;  //bump past last boundary.  There might be more traffic after this.
                return i;
            } else if (line.startsWith(DD + traffic.boundary)){   //this is a first or middle boundary, but not last boundary.
                i++;  //bump past boundary
                //begin payload chunk
                Part part = new Part(traffic.boundary);
                traffic.payloads.add(part);
                i = part.readPart(lines, i);
            } else {
                return i;
                //if (line.trim().length()>0){
                //    System.err.println("********** Skipping line: "+line); //either parser error, or something is outside of a boundary.
                //}
                //i++;
            }
        }
        return i;
    }

         
    private HttpTraffic parseForward(String forward, int nexti){
        HttpTraffic forwardTraffic = new HttpTraffic();
        forwardTraffic.isRequest = true;
        forwardTraffic.ID = getID();
        //String[] lines = forward.split("\\r\\n", -1);
        String[] lines = forward.split("\\n", -1);
        int lineCount = lines.length;
        String line;
        int i = nexti;

        // Read the first line, and figure out if GET, POST, etc., and the URI
        line = lines[i];
        while (line.trim().length()==0){
            i++;
            if (i>=lineCount-1){
                return null;
            }
            line = lines[i];
        }
        String[] tokens = line.split(" ", -1);
        forwardTraffic.method = tokens[0];
        String urlString = tokens[1];
        String[] urlHalves = urlString.split("\\?", -1); //look for a query string of the form /foo/bar?param=baz and break on question mark.
        forwardTraffic.url = urlHalves[0];
        if (urlHalves.length > 1){
            forwardTraffic.queryParams = urlHalves[1];
        }
        i++;

        //if (forwardTraffic.method.equals("GET")|| forwardTraffic.method.equals("DELETE")){
        //    return forwardTraffic;
        //}
        // Now read headers until we are ready for payload or parts.
        i = readHeaders(forwardTraffic, lines, i);

        /*
        if ( (i<lines.length-1) && (forwardTraffic.contentLength<=0) ) {  //0 means a 0 was seen, -1 means no header was seen, as will be the case in GET or DELETE.

            //there are more lines, but content-length header was zero,
            // this means we are getting keep-alive bunches of DELETEs or OKs back.
            System.err.println("###### extra requests in this one."+getID());
            String filename = getID()+'_'+forwardTraffic.method+'_'+forwardTraffic.url.replaceAll("/", "_")+".requests";
            saveFile("./xml", filename, forward);
            return forwardTraffic;
        }
        */

        // We are past headers now. The rest of message is payloads.
        i = readPayloads(forwardTraffic, lines, i);  //messes with forwardTraffic and places parts in it.
        forwardTraffic.nexti = i;
        return forwardTraffic;
    }

    private HttpTraffic parseReverse(String reverse, int nexti){
        HttpTraffic reverseTraffic = new HttpTraffic();
        reverseTraffic.isRequest = false;
        reverseTraffic.ID = getID();
        //String[] lines = reverse.split("\\r\\n", -1);
        String[] lines = reverse.split("\\n", -1);
        int lineCount = lines.length;
        String line;
        int i = nexti;
        if (i>=lineCount){
            return null;
        }
        line = lines[i];
                   
        // Read the first line, and figure out response code, message.
        while (i<lineCount){
            if (line.startsWith("HTTP/1.1")){
                break;
            }
            i++;
            line = lines[i];
        }
        String[] tokens = line.split(" ", 3);
        String HTTP11 = tokens[0];
        reverseTraffic.responseCode = Integer.parseInt(tokens[1]);
        reverseTraffic.message = killTrailingWS(tokens[2]);
        i++;  // done reading first line. Bump past first line.

        //if (forwardResult.message.equals("OK")){
        //    return forwardResult;
        //}

        // Now read headers until we are ready for payload or parts.
        i = readHeaders(reverseTraffic, lines, i);

        /*
        if ( (i<lines.length-1) && (reverseTraffic.contentLength==0) ) {
            //there are more lines, but content-length header was zero,
            // this means we are getting keep-alive bunches of DELETEs or OKs back.
            System.err.println("###### extra responses in this one."+id);
            String filename = getID()+".reponses";
            saveFile("./xml", filename, reverse);
            reverseTraffic.extra = true;
            return reverseTraffic;
        }
        */
        // We are past headers now. The rest of message is payloads.
        i = readPayloads(reverseTraffic, lines, i);  //messes with forwardResult and places parts in it.
        reverseTraffic.nexti = i;
        if (i>=lineCount){
            reverseTraffic.nexti = -1;
        }
        return reverseTraffic;
    }

    private List<HttpTraffic> handleTcpDump(String dump){
        int i = 0;
        int trafficID = 0;
        List<HttpTraffic> trafficList = new ArrayList<HttpTraffic>();
        while (i>-1){
            trafficID++;
            HttpTraffic forward = parseForward(dump, i);
            if (forward==null) break;
            i = forward.nexti;
            forward.ID = ""+trafficID;
            if (forward.payloads.size()>0){
                saveForwardFiles(forward);
            }
            trafficList.add(forward);

            HttpTraffic reverse = parseReverse(dump, i);
            if (reverse==null) break;
            reverse.ID = ""+trafficID;
            i = reverse.nexti;
            if (reverse.payloads.size()>0){
                saveReverseFiles(reverse);
            }
            trafficList.add(reverse);
        }
        return trafficList;
    }

    public static File saveFile(String dir, String relativeName, String content){
        File result = null;
        PrintWriter writer;
        try{
            result = new File(dir, relativeName);
            writer = new PrintWriter(new FileOutputStream(result));
        }catch (Exception e){
            System.out.println("Can't write to file in saveFile: " + relativeName + "  \r\n" + e);
            return null;
        }
        writer.write(content);
        writer.close();
        return result;
    }
    
    private void saveForwardFiles(HttpTraffic fr){
        for (Part part : fr.payloads){
            String body = part.buffer.toString();
            if (body.trim().length()==0){
                continue;
            }
            String filename = fr.ID+'_'+fr.method+'_'+fr.url.replaceAll("/", "_")+'_'+part.label+".xml";
            filename = filename.replaceAll("/", "_");
            System.out.println("trying to save file: "+filename+" :: "+fr);
            part.filename = filename;
            saveFile("./xml", filename, body);
        }
    }
    
    private void saveReverseFiles(HttpTraffic fr){
        for (Part part : fr.payloads){
            String body = part.buffer.toString();
            if (body.trim().length()==0){
                continue;
            }
            String filename = fr.ID+'_'+fr.method+'_'+fr.url.replaceAll("/", "_");
            if (part.label.length()==0){
                if (body.trim().startsWith("<?xml")){
                    filename = filename + "_res.xml";
                    part.filetype = "xml";
                } else {
                    filename = filename + "_res.txt";
                    part.filetype = "txt";
                }
            } else {
                filename = filename + '_'+part.label+"_res.xml";
                part.filetype = "xml";
            }
            filename = filename.replaceAll("/", "_");
            System.out.println("trying to save file: "+filename+" :: "+fr);
            part.filename = filename;
            saveFile("./xml", filename, body);
        }
    }

    public static String readFile(String dir, String relPath) throws Exception{
        File theFile = new File(dir, relPath);
        FileInputStream fis = new FileInputStream(theFile);
        byte[] theData = new byte[(int) theFile.length()];
        // need to check the number of bytes read here
        int howmany = fis.read(theData);
        fis.close();
        return new String(theData);
    }

    public static List<HttpTraffic> process(String httpSessionTraffic){
        PayloadLogger pll = new PayloadLogger();
        List<HttpTraffic> trafficList = pll.handleTcpDump(httpSessionTraffic);
        return trafficList;
    }

    public static void main(String[]args) throws Exception {
        String dump = readFile(".", args[0]);
        PayloadLogger pll = new PayloadLogger();
        List<HttpTraffic> trafficList = pll.handleTcpDump(dump);
        reporter.finished(trafficList);
        saveReport();
    }

    
}