package org.collectionspace.services.client;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.collectionspace.services.common.api.Tools;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlTools {

    static final Logger logger = LoggerFactory.getLogger(XmlTools.class);

    // @TODO Refactoring opportunity: the utility methods below
    // could potentially be moved into the 'common' module,
    // and made static and public.
    //     -- DONE.   Moved here.  Laramie20110519

    // Output format for XML pretty printing.
    public final static OutputFormat PRETTY_PRINT_OUTPUT_FORMAT =  defaultPrettyPrintOutputFormat();
    public final static String UTF8_ENCODING = "UTF-8";


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
     * Returns a dom4j XML document, when provided with a String
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
    
    
	public static String asXML(Document doc, boolean suppressDeclaration) {
		OutputFormat format = new OutputFormat();
		format.setSuppressDeclaration(suppressDeclaration);
		format.setEncoding(UTF8_ENCODING);

		try {
			StringWriter out = new StringWriter();
			XMLWriter writer = new XMLWriter(out, format);
			writer.write(doc);
			writer.flush();

			return out.toString();
		} catch (IOException e) {
			throw new RuntimeException("IOException while generating textual "
					+ "representation: " + e.getMessage());
		}
	}
    
    /**
     * Returns a dom4j XML document, when provided with a file
     * containing a well-formed XML document.
     * @param   file  A file containing a well-formed XML document.
     * @return  A dom4j XML document.
     */
    public static Document fileToXMLDocument(File file) throws Exception {
        Document doc = null;
        try {
            SAXReader reader = new SAXReader();
            doc = reader.read(file);
        } catch (DocumentException e) {
            throw e;
        }
        return doc;
    }
    
    /**
     * Writes a dom4j XML document to a file on disk. Uses UTF-8 character
     * encoding.
     * @param  doc  A dom4j XML document.
     * @param  file  A file.
     */
    public static void xmlDocumentToFile(Document doc, File file) throws Exception {
        if (doc == null) {
            System.err.println("Document is null");
            return;
        }
        FileWriter filewriter = null;
        try {
            filewriter = new FileWriter(file);
            // asXML() appears to output an adequate serialization, thus
            // obviating the need to use an XML-aware writer here.
            filewriter.write(doc.asXML());
            filewriter.flush();
            filewriter.close();
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
            throw e;
        } finally {
            closeQuietly(filewriter);
       }
    }
    
    /**
     * Attempt to close a resource, swallowing any Exceptions thrown.
     * This method should only be called from within the 'finally' portion
     * of a 'catch/try/finally' block.
     * See http://stackoverflow.com/questions/2699209/java-io-ugly-try-finally-block
     * and http://stackoverflow.com/questions/341971/what-is-the-execute-around-idiom
     * @param c A closeable resource.
     */
    public static void closeQuietly(Closeable c) {
        if (c != null) try {
            c.close();
        } catch(Exception e) {
            // Do nothing here
        }
    }
    
    /**
     * Returns the (text node) value of a specified element in a dom4j XML document.
     * @param   doc  A dom4j XML document.
     * @param   xpathExpr  An XPath expression intended to match a single element
     * in the XML document, in the default namespace.
     * @return  The (text node) value of the matched element, if any.
     */
    public static String getElementValue(Document doc, String xpathExpr) {
        String elementValue = "";
        if (Tools.isBlank(xpathExpr)) {
            return elementValue;
        }
        try {
            Node node = doc.selectSingleNode(xpathExpr);
            if ((node == null) || (node.getNodeType() != Node.ELEMENT_NODE)) {
                return elementValue;
            }
            Element element = (Element) node;
            elementValue = element.getText();
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
        } finally {
            return elementValue;
        }
    }
    
    public static String getElementValue(Element doc, String xpathExpr) {
        String elementValue = "";
        if (Tools.isBlank(xpathExpr)) {
            return elementValue;
        }
        try {
            Node node = doc.selectSingleNode(xpathExpr);
            if ((node == null) || (node.getNodeType() != Node.ELEMENT_NODE)) {
                return elementValue;
            }
            Element element = (Element) node;
            elementValue = element.getText();
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
        } finally {
            return elementValue;
        }
    }
    
    /**
     * Returns the (text node) value of a specified element in a dom4j XML document.
     * @param   doc  A dom4j XML document.
     * @param   xpathExpr  An XPath expression intended to match a single element
     * in the XML document, in the default namespace.
     * @return  The (text node) value of the matched element, if any.
     */
    public static List getElementNodes(Document doc, String xpathExpr) {
        List result = null;

        try {
            result = doc.selectNodes(xpathExpr);
        } catch (Exception e) {
            logger.debug("No list-item nodes exist.");
        }
        
        return result;
    }
    
    /**
     * Sets the (text node) value of a specified element in a dom4j XML document.
     * @param   doc  A dom4j XML document.
     * @param   xpathExpr  An XPath expression intended to match a single element
     * in the XML document, in the default namespace.
     * @param   elementValue  The value that the element should contain. If this
     * provided value is null, the element's value will be set to an empty string.
     * @return  The document with the (text node) value of the matched element, if
     * any, set to the provided value.
     */
    public static Document setElementValue(Document doc, String xpathExpr,
            String elementValue) {
        if (Tools.isBlank(xpathExpr)) {
            return doc;
        }
        try {
            Node node = doc.selectSingleNode(xpathExpr);
            if ((node == null) || (node.getNodeType() != Node.ELEMENT_NODE)) {
                return doc;
            }
            Element element = (Element) node;
            element.setText(elementValue == null ? "" : elementValue);
            return doc;
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
        } finally {
            return doc;
        }
    }
    
    /**
     * Sets the value of a specified attribute in a dom4j XML document.
     * @param   doc  A dom4j XML document.
     * @param   xpathExpr  An XPath expression intended to match a single element
     * in the XML document, in the default namespace.
     * @param   attributeName  An attribute name.
     * @param   attributeValue  The value that the attribute should contain.
     * @return  The document with the specified attribute of the matched element, if
     * any, set to the provided value. If the attribute doesn't already exist,
     * it will be created and assigned the provided value.
     */
    public static Document setAttributeValue(Document doc, String xpathExpr,
            String attributeName, String attributeValue) {
        if (Tools.isBlank(xpathExpr) || Tools.isBlank(attributeName)) {
            return doc;
        }
        try {
            Node node = doc.selectSingleNode(xpathExpr);
            if ((node == null) || (node.getNodeType() != Node.ELEMENT_NODE)) {
                return doc;
            }
            Element element = (Element) node;
            element.addAttribute(attributeName, attributeValue == null ? "" : attributeValue);
            return doc;
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
        } finally {
            return doc;
        }
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
