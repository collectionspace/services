package org.collectionspace.services.IntegrationTests.xmlreplay;

import org.collectionspace.services.common.XmlSaxFragmenter;
import org.collectionspace.services.common.api.Tools;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

/**  Format a report based on XmlReplay ServiceResult object from a test group.
 * @author  laramie
 */
public class XmlReplayReport {
    protected static final String HTML_PAGE_START = "<html><body>";
    protected static final String HTML_PAGE_END = "</body></html>";

    protected static final String HTML_TEST_START = "<div class='TESTCASE'>";
    protected static final String HTML_TEST_END = "</div>";

    protected static final String HTML_PAYLOAD_START = "<div class='PAYLOAD'><pre>";
    protected static final String HTML_PAYLOAD_END = "</pre></div>";

    protected static final String PRE_START = "<pre class='SUMMARY'>";
    protected static final String PRE_END = "</pre>";

    protected static final String DETAIL_START = "<table border='1'><tr><td>\r\n";
    protected static final String DETAIL_LINESEP = "</td></tr>\r\n<tr><td>";
    protected static final String DETAIL_END = "</td></tr></table>";



    private StringBuffer buffer = new StringBuffer();

    public void init(){
        buffer.append(HTML_PAGE_START);
    }

    public void finish(){
        buffer.append(HTML_PAGE_END);
    }

    public String getPage(){
        return buffer.toString();
    }

    public void addText(String text){
         buffer.append(text);
    }

    public void addTestResult(ServiceResult serviceResult){
        buffer.append(HTML_TEST_START);
        buffer.append(formatSummary(serviceResult));
        buffer.append(formatPayloads(serviceResult));
        buffer.append(HTML_TEST_END);
    }

    protected String formatSummary(ServiceResult serviceResult){
        StringBuffer fb = new StringBuffer();
        //fb.append(PRE_START);
        fb.append(detail(serviceResult, false, false, DETAIL_START, DETAIL_LINESEP, DETAIL_END));
        //fb.append(PRE_END);
        return fb.toString();
    }

    protected String formatPayloads(ServiceResult serviceResult){
        StringBuffer fb = new StringBuffer();

        String reqRaw = serviceResult.requestPayloadsRaw;      //TODO: When there is a div to collapse these, include this un-expanded payload as well.
        String req = serviceResult.requestPayload;
        appendPayload(fb, req, "REQUEST");

        String resp = serviceResult.result;
        appendPayload(fb, resp, "RESPONSE");

        return fb.toString();
    }

    protected void appendPayload( StringBuffer fb , String payload, String title){
        if (Tools.notBlank(payload)){
            fb.append(HTML_PAYLOAD_START);
            fb.append(title+":\r\n");
            try {
                String pretty = prettyPrint(payload);
                fb.append(escape(pretty));
            } catch (Exception e){
                String error = "<font color='red'>ERROR pretty printing requestPayload"+e+"</font> "+payload;
                fb.append(error);
            }
            fb.append(HTML_PAYLOAD_END);
        }
    }

    private String escape(String source){
        StringBuffer fb = new StringBuffer();
        try {
            String escaped = Tools.searchAndReplace(source, "<", "&lt;");
            fb.append(escaped);
            fb.append(HTML_PAYLOAD_END);
        } catch (Exception e){
            fb.append("ERROR escaping requestPayload"+e);
        }
        return fb.toString();
    }

    private String prettyPrint(String rawXml) throws Exception {
        Document document = DocumentHelper.parseText(rawXml);
        return XmlSaxFragmenter.prettyPrint(document, true);
    }

    private static final String LINE = "<hr />\r\n";
    private static final String CRLF = "<br />\r\n";

    public String detail(ServiceResult s, boolean includePayloads, boolean includePartSummary, String start, String linesep, String end){
        String res =  start
                + ( s.gotExpectedResult() ? "SUCCESS" : "<font color='red'>FAILURE</font>"  )                                    +linesep
                + (Tools.notBlank(s.failureReason) ? s.failureReason +linesep : "" )
                +s.method                                                                                           +linesep
                +s.responseCode                                                                                  +linesep
                + ( (s.expectedCodes.size()>0) ? "expectedCodes:"+s.expectedCodes+linesep : "" )
                + ( Tools.notEmpty(s.testID) ? "testID:"+s.testID+linesep : "" )
                + ( Tools.notEmpty(s.testGroupID) ? "testGroupID:"+s.testGroupID+linesep : "" )
                + ( Tools.notEmpty(s.fromTestID) ? "fromTestID:"+s.fromTestID+linesep : "" )
                + ( Tools.notEmpty(s.responseMessage) ? "msg:"+s.responseMessage+linesep : "" )
                +"URL:<a href='"+s.fullURL+"'>"+s.fullURL+"</a>"                                     +linesep
                +"auth: "+s.auth                                                                                        +linesep
                + ( Tools.notEmpty(s.deleteURL) ? "deleteURL:"+s.deleteURL+linesep : "" )
                + ( Tools.notEmpty(s.location) ? "location.CSID:"+s.location+linesep : "" )
                + ( Tools.notEmpty(s.error) ? "ERROR:"+s.error +linesep : "" )
                + "gotExpected:"+s.gotExpectedResult()                                                       +linesep
                + "part summary: "+( s.partsSummary(includePartSummary))                                                                             +linesep
                + ( includePayloads && Tools.notBlank(s.requestPayload) ? LINE+"requestPayload:"+LINE+CRLF+s.requestPayload+LINE : "" )
                + ( includePayloads && Tools.notBlank(s.result) ? LINE+"result:"+LINE+CRLF+s.result : "" )
                +end;
        return res;
    }

}
