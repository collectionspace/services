/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2011 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.common;

import org.collectionspace.services.common.api.Tools;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.ext.Locator2;
import java.io.StringWriter;

/** Use XmlSaxFragmenter to parse a large file or InputSource (stream)
 *   with SAX, and pass in an instance of IFragmentHandler to parse() so that you can
 *   get fragments back that you can parse with DOM or other processing.
 *
 *   You would typically instantiate and run this class like so:
 *
 *   IFragmentHandler callback = new MyFragmentHandlerImpl();  //define the interface somewhere.
 *   XmlSaxFragmenter.parse("C:\\tmp\\imports.xml", "/document/schema", callback);
 *
 *   Then, given an XML document like this:
 *       &lt;document repository="default" id="123">
 *         &lt;schema name="collectionobjects_naturalhistory">
 *           &lt;nh-int/>
 *           &lt;nh-note/>
 *         &lt;/schema>
 *         &lt;schema name="collectionobjects_common">
 *            &lt;distinguishingFeatures/>
 *         &lt;/schema>
 *       &lt;/document>
 *
 *    you'll get two onFragmentReady() events: the first will pass String fragment =
 *        &lt;schema name="collectionobjects_naturalhistory">            &lt;nh-int/>
 *        &lt;nh-note/>
 *    plus some context information, and the second will pass String fragment =
 *        &lt;distinguishingFeatures/>
 *
 *
 * @author Laramie Crocker
 */
public class XmlSaxFragmenter implements ContentHandler, ErrorHandler {

    //=============== ContentHandler ====================================================

    public void setDocumentLocator(Locator locator) {
        if (xmlDeclarationDone){
            return;
        }
        if (locator instanceof Locator2){
            Locator2 l2 = ((Locator2) locator);
            String enc = l2.getEncoding();
            String ver = l2.getXMLVersion();
            append("<?xml version=\""+ver+"\" encoding=\""+enc+"\"?>\r\n");
            xmlDeclarationDone = true;
        } else {
            //System.err.println("Locator2 not found.");
            append("<?xml version=\"1.0\"?>\r\n");
            xmlDeclarationDone = true;
        }
        //more info available from Locator if needed: locator.getPublicId(), locator.getSystemId();
    }

    public void startDocument() throws SAXException {
        document = DocumentHelper.createDocument();
    }

    public void endDocument() throws SAXException {
        if (fragmentHandler!=null) {
            fragmentHandler.onEndDocument(document, fragmentIndex + 1);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        String attsString = attsToStr(atts);
        append("<" + name(qName, localName) + attsString + ">");
        if (inFragment){
            inFragmentDepth++;
            return;
        }
        if (currentElement == null){
            currentElement =  document.addElement(qName);
        } else {
            Element element = DocumentHelper.createElement(qName);
            currentElement.add(element);
            previousElement = currentElement;
            currentElement = element;
        }
        addAttributes(currentElement, atts);
        String currentPath = currentElement.getPath();
        if (currentPath.equals(chopPath)){
            buffer = new StringBuffer();
            inFragment = true;
            if (includeParent){
                append("<" + name(qName, localName) + attsString + ">");
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (inFragment && (inFragmentDepth>0)){
            append("</" + name(qName, localName) + '>');
        } else if (inFragment && inFragmentDepth == 0 && includeParent){
            append("</" + name(qName, localName) + '>');
        }
        if (inFragment && (inFragmentDepth==0)){
            if (fragmentHandler!=null) {
                fragmentIndex++;
                fragmentHandler.onFragmentReady(document,
                                                currentElement,
                                                currentElement.getPath(),
                                                fragmentIndex,
                                                buffer.toString());
            }
            inFragment = false;
            currentElement = previousElement;
        }
        if (inFragment){
            inFragmentDepth--;
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        String chars = new String(ch, start, length);
        append(chars);
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }
    public void endPrefixMapping(String prefix) throws SAXException {
    }
    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
    }
    public void processingInstruction(String target, String data) throws SAXException {
    }
    public void skippedEntity(String name) throws SAXException {
    }

    //=============== ErrorHandler ====================================================

    public void error(SAXParseException exception){
        System.err.println("ERROR:"+exception);
    }
    public void fatalError(SAXParseException exception){
        System.err.println("FATAL_ERROR:"+exception);
    }
    public void warning(SAXParseException exception){
        System.err.println("WARNING:"+exception);
    }

    //================ Helper Methods ===================================================

    private Document document;
    private Element currentElement;
    private Element previousElement;
    private StringBuffer buffer = new StringBuffer();

    private boolean xmlDeclarationDone = false;
    private boolean inFragment = false;
    private int inFragmentDepth = 0;
    private int fragmentIndex = -1;  //zero-based.  Used for informational purposes only, to report to the IFragmentHandler.

    private String chopPath = "";
    public String getChopPath() {
        return chopPath;
    }
    /** You should not set the chopPath directly; instead you must set it in the call to parse(). */
    protected void setChopPath(String chopPath) {
        this.chopPath = chopPath;
    }

    private boolean includeParent = false;
    public boolean isIncludeParent() {
        return includeParent;
    }
    public void setIncludeParent(boolean includeParent) {
        this.includeParent = includeParent;
    }


    private IFragmentHandler fragmentHandler;
    public IFragmentHandler getFragmentHandler() {
        return fragmentHandler;
    }
    /** You should not set the FragmentHandler directly; instead you must set it in the call to parse(). */
    protected void setFragmentHandler(IFragmentHandler fragmentHandler) {
        this.fragmentHandler = fragmentHandler;
    }

    protected void append(String str){
        buffer.append(str);
    }

    protected String name(String qn, String ln){
        if (Tools.isEmpty(qn)){
            return ln;
        }
        if (qn.equals(ln)){
            return ln;
        }
        return qn;
    }

    //NOTE: we don't deal with this here because we don't need to
    // actually understand the namespace uri:
    // a.getURI(i)
    protected String attsToStr(Attributes a){
        StringBuffer b = new StringBuffer();
        String qn, ln;
        int attsLen = a.getLength();
        for (int i=0; i<attsLen; i++){
            b.append(' ');
            qn = a.getQName(i);
            ln = a.getLocalName(i);
            b.append(name(qn, ln)).append("=\"")
             .append(a.getValue(i)).append('\"');
        }
        return b.toString();
    }

    protected void addAttributes(Element cur, Attributes a){
        int attsLen = a.getLength();
        for (int i=0; i<attsLen; i++){
            cur.addAttribute(a.getQName(i), a.getValue(i));
        }
    }


    /** This method takes a filename of a local file only; InputSource is not implemented yet.
     *
     * @param theFileName the filename of a local file, which should be valid XML.
     * @param chopPath    the path from the root of the document to the parent element
     *                    of the fragment you want.
     * @param handler     An instance of IFragmentHandler that you define to get the onFragmentReady event
     *                    which will give you the fragment and some context information.
     * @param includeParent  If you set this to true, you will get the element described by chopPath included in the fragment, otherwise,
     *                       it will not appear in the fragment; in either case, the element will be available in the Document context and the
     *                       Element fragmentParent in the callback IFragmentHandler.onFragmentReady().
     */
    public static void parse(String theFileName,
                             String chopPath,
                             IFragmentHandler handler,
                             boolean includeParent){
        try{
            XMLReader parser = setupParser(chopPath, handler, includeParent);
            parser.parse(theFileName);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void parse(InputSource inputSource,
                             String chopPath,
                             IFragmentHandler handler,
                             boolean includeParent){
        try{
            XMLReader parser = setupParser(chopPath, handler, includeParent);
            parser.parse(inputSource);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    protected static XMLReader setupParser(String chopPath,
                                      IFragmentHandler handler,
                                      boolean includeParent) throws Exception {
            XMLReader parser = XMLReaderFactory.createXMLReader();
            XmlSaxFragmenter fragmenter = new XmlSaxFragmenter();
            fragmenter.setChopPath(chopPath);
            fragmenter.setFragmentHandler(handler);
            fragmenter.setIncludeParent(includeParent);
            parser.setContentHandler(fragmenter);
            parser.setErrorHandler(fragmenter);
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            return parser;
    }

}
