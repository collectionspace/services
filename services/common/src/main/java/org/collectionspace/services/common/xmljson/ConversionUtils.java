package org.collectionspace.services.common.xmljson;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility methods for doing XML/JSON conversion.
 */
public class ConversionUtils {
    /**
     * Prefix to prepend to XML attribute names when converting to JSON
     * field names.
     */
    public static final String XML_ATTRIBUTE_PREFIX = "@";

    /**
     * Prefix to prepend to XML namespace prefixes when converting to
     * JSON field names.
     */
    public static final String XML_NAMESPACE_PREFIX = XML_ATTRIBUTE_PREFIX + "xmlns:";

    /**
     * Converts an XML attribute name to a JSON field name.
     *
     * @param xmlAttributeName the XML attribute name
     * @return the JSON field name
     */
    public static String xmlAttributeNameToJsonFieldName(String xmlAttributeName) {
        return XML_ATTRIBUTE_PREFIX + xmlAttributeName;
    }

    /**
     * Converts a JSON field name to an XML attribute name.
     * The field name must be in the expected format, as determined by
     * isXmlAttribute().
     *
     * @param jsonFieldName the JSON field name
     * @return the XML attribute name
     */
    public static String jsonFieldNameToXmlAttributeName(String jsonFieldName) {
        return jsonFieldName.substring(XML_ATTRIBUTE_PREFIX.length());
    }

    /**
     * Converts an XML namespace prefix to a JSON field name.
     *
     * @param xmlNamespacePrefix the XML namespace prefix
     * @return the JSON field name
     */
    public static String xmlNamespacePrefixToJsonFieldName(String xmlNamespacePrefix) {
        return XML_NAMESPACE_PREFIX + xmlNamespacePrefix;
    }

    /**
     * Converts a JSON field name to an XML namespace prefix.
     * The field name must be in the expected format, as determined by
     * isXmlNamespace().
     *
     * @param jsonFieldName the JSON field name
     * @return the XML namespace prefix
     */
    public static String jsonFieldNameToXmlNamespacePrefix(String jsonFieldName) {
        return jsonFieldName.substring(XML_NAMESPACE_PREFIX.length());
    }

    /**
     * Determines if a JSON field name represents an XML
     * attribute.
     *
     * @param jsonFieldName the field name to test
     * @return true if the field name represents an XML attribute,
     *         false otherwise
     */
    public static boolean isXmlAttribute(String jsonFieldName) {
        return jsonFieldName.startsWith(XML_ATTRIBUTE_PREFIX);
    }

    /**
     * Determines if a JSON field name represents an XML
     * namespace prefix.
     *
     * @param jsonFieldName the field name to test
     * @return true if the field name represents an XML namespace prefix,
     *         false otherwise
     */
    public static boolean isXmlNamespace(String jsonFieldName) {
        return jsonFieldName.startsWith(XML_NAMESPACE_PREFIX);
    }

    /**
     * Determines if a JSON field name represents XML element content.
     *
     * @param jsonFieldName the field name to test
     * @return true if the field name represents XML element content,
     *         false otherwise
     */
    public static boolean isXmlContent(String jsonFieldName) {
        return jsonFieldName.equals(".");
    }

    /**
     * Converts an XML element QName to a JSON field name.
     *
     * @param name the XML element QName
     * @return the JSON field name
     */
    public static String jsonFieldNameFromXMLQName(QName name) {
        String prefix = name.getPrefix();
        String localPart = name.getLocalPart();

        if (StringUtils.isNotEmpty(prefix)) {
            return prefix + ":" + localPart;
        }

        return localPart;
    }
}
