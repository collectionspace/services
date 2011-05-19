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

    protected static final String HTML_PAYLOAD_START = "<div class='PAYLOAD'>";
    protected static final String HTML_PAYLOAD_END = "</div>";

    protected static final String PRE_START = "<pre class='SUMMARY'>";
    protected static final String PRE_END = "</pre>";

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

    public void addTestResult(ServiceResult serviceResult){
        buffer.append(HTML_TEST_START);
        buffer.append(formatSummary(serviceResult));
        buffer.append(formatPayloads(serviceResult));
        buffer.append(HTML_TEST_END);
    }

    protected String formatSummary(ServiceResult serviceResult){
        StringBuffer fb = new StringBuffer();
        fb.append(PRE_START);
        fb.append(serviceResult.detail(false));
        fb.append(PRE_END);
        return fb.toString();
    }

    protected String formatPayloads(ServiceResult serviceResult){
        StringBuffer fb = new StringBuffer();

        fb.append(HTML_PAYLOAD_START);
        String req = serviceResult.requestPayloadsRaw;
        fb.append(escape(req));
        fb.append(HTML_PAYLOAD_END);

        fb.append(HTML_PAYLOAD_START);
        String resp = serviceResult.result;
        fb.append(escape(resp));
        fb.append(HTML_PAYLOAD_END);

        return fb.toString();
    }

    private String escape(String source){
        StringBuffer fb = new StringBuffer();
        try {
            String pretty = prettyPrint(source);
            String escaped = Tools.searchAndReplace(pretty, "<", "&lt;");
            fb.append(escaped);
            fb.append(HTML_PAYLOAD_END);
        } catch (Exception e){
            fb.append("ERROR converting requestPayload"+e);
        }
        return fb.toString();
    }

    private String prettyPrint(String rawXml) throws  Exception {
        Document document = DocumentHelper.parseText(rawXml);
        return XmlSaxFragmenter.prettyPrint(document);
    }

}
