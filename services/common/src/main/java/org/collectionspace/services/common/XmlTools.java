package org.collectionspace.services.common;

import org.collectionspace.services.common.api.Tools;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.StringWriter;

public class XmlTools {


    // @TODO Refactoring opportunity: the utility methods below
    // could potentially be moved into the 'common' module,
    // and made static and public.
    //     -- DONE.   Moved here.  Laramie20110519

    // Output format for XML pretty printing.
    public final static OutputFormat PRETTY_PRINT_OUTPUT_FORMAT =  defaultPrettyPrintOutputFormat();


    /**
     * Returns a default output format for pretty printing an XML document.
     *
     * Uses the default settings for indentation, whitespace, etc.
     * of a pre-defined dom4j output format.
     *
     * @return  A default output format for pretty printing an XML document.
     */
    protected static OutputFormat defaultPrettyPrintOutputFormat() {

        // Use the default pretty print output format in dom4j.
        OutputFormat outformat = OutputFormat.createPrettyPrint();
        // Supress the extra newline added after the XML declaration
        // in that output format.
        outformat.setNewLineAfterDeclaration(false);
        return outformat;
    }

    /** Returns a pretty printed String representation of an XML document.
     * @param   doc        A dom4j XML Document.
     * @return  A pretty printed String representation of an XML document.
     */
    public static String prettyPrintXML(Document doc) {

        String xmlStr = "";
        try {
          xmlStr = formatXML(doc, PRETTY_PRINT_OUTPUT_FORMAT);
        // If an error occurs during pretty printing, fall back to
        // returning a default String representation of the XML document.
        } catch (Exception e) {
            System.err.println("Error pretty-printing XML: " + e.getMessage());
            xmlStr = doc.asXML();
        }

        return xmlStr;
    }

    /**
     * Returns a String representation of an XML document,
     * formatted according to a specified output format.
     * @param   doc        A dom4j XML Document.
     * @param   outformat  A dom4j output format.
     * @return  A String representation of an XML document,
     *          formatted according to the specified output format.
     * @throws Exception if an error occurs in printing
     *          the XML document to a String.
     */
    public static String formatXML(Document doc, OutputFormat outformat)
       throws Exception {

        StringWriter sw = new StringWriter();
        try {
            final XMLWriter writer = new XMLWriter(sw, outformat);
            // Print the document to the current writer.
            writer.write(doc);
        }
        catch (Exception e) {
            throw e;
        }
        return sw.toString();
    }

    /**
     * Returns an XML document, when provided with a String
     * representation of that XML document.
     * @param   xmlStr  A String representation of an XML document.
     * @return  A dom4j XML document.
     */
    public static Document textToXMLDocument(String xmlStr) throws Exception {

        Document doc = null;
        try {
         doc = DocumentHelper.parseText(xmlStr);
        } catch (DocumentException e) {
          throw e;
        }
        return doc;
    }

    public static String prettyPrint(String xml) throws Exception {
        Document doc = textToXMLDocument(xml);
        return prettyPrint(doc,  "    ");
    }

    public static String prettyPrint(Document document) {
        return prettyPrint(document, null);
    }

    public static String prettyPrint(Document document, String indentString) {
        String prettyHTML;
        try {
            StringWriter swriter = new StringWriter();
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setNewlines(true);
            format.setTrimText(true);
            boolean indent = Tools.notEmpty(indentString);
            format.setIndent(indent);
            if (indent){
                format.setIndent(indentString);
            }
            format.setXHTML(true);
            format.setLineSeparator(System.getProperty("line.separator")) ;
            HTMLWriter writer = new HTMLWriter(swriter, format);
            writer.write(document);
            writer.flush();
            prettyHTML = swriter.toString();
        } catch (Exception e){
            prettyHTML = "<?xml?><error>"+e+"</error>";
        }
        return prettyHTML;
    }
}
