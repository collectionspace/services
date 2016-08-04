package org.collectionspace.services.common.xmljson;

import static org.collectionspace.services.common.xmljson.ConversionUtils.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * <p>A lightweight representation of an XML node. Instances are created
 * by XmlToJsonStreamConverter in the course of parsing XML. This class
 * differs from a DOM node in that it is intended to contain just the
 * information needed to generate JSON for CSpace, in a structure that
 * is optimized for doing that generation.</p>
 * 
 * <p>Each XML node has a name, and optionally namespaces and attributes.
 * The node may contain either text or child nodes (not both, as CSpace
 * XML is assumed not to contain mixed-content elements).</p>
 */
public class XmlNode {
    /**
     * The name of the node.
     */
    private String name;
    
    /**
     * The text content of the node, if this is a text node.
     */
    private String text = "";
    
    /**
     * The namespaces (prefix and uri) of the node, keyed by prefix.
     */
    private Map<String, String> namespaces = new LinkedHashMap<String, String>();
    
    /**
     * The attributes (name and value) of the node, keyed by name.
     */
    private Map<String, String> attributes = new LinkedHashMap<String, String>();
    
    /**
     * The children of the node, keyed by name. A child may be a single XmlNode,
     * or a list of XmlNodes, if more than one child has the same name.
     */
    private Map<String, Object> children = new LinkedHashMap<String, Object>();
    
    /**
     * Is text is allowed in this node? This starts off as true.
     * Adding a child node causes it to become false.
     */
    private boolean isTextAllowed = true;
    
    /**
     * Should empty children be retained? If false, children that
     * contain no content at the time of addition are not retained.
     */
    private boolean isRetainEmptyChildren = true;
    
    /**
     * Creates an XmlNode.
     */
    public XmlNode() {
        
    }

    /**
     * Creates an XmlNode with a name.
     * 
     * @param name the name
     */
    public XmlNode(String name) {
        setName(name);
    }
    
    /**
     * <p>Gets the value of the node. If this is a text node, the
     * value is a String. Otherwise it's a map of the node's
     * namespaces, attributes, and children, via
     * getCombinedMap().</p>
     * 
     * <p>Note that namespaces and attributes are not returned
     * as part of a text node's value. It is assumed that text
     * nodes do not have namespace declarations or attributes.</p>
     * 
     * @return the node's value
     */
    @JsonValue
    public Object getValue() {
        if (hasChildren()) {
            return getCombinedMap();
        }
            
        if (hasText()) {
            return getText();
        }
        
        return null;
    }
    
    /**
     * Determines if this node has content. A node has
     * content if it contains non-empty text, or if it has
     * any children.
     * 
     * @return true if the node has no content, false otherwise
     */
    public boolean isEmpty() {
        return (!(hasChildren() || hasText()));
    }
    
    /**
     * Returns a map containing the node's namespaces, attributes, and
     * children. The keys for namespaces and attributes are computed
     * using ConversionUtils.xmlNamespacePrefixToJsonFieldName() and
     * ConversionUtils.xmlNamespacePrefixToJsonFieldName() respectively.
     * Children are keyed by their names.
     * 
     * @return a map of namespaces, attributes, and children
     */
    public Map<String, Object> getCombinedMap() {
        Map<String, Object> combined = new LinkedHashMap<String, Object>();
        Map<String, String> namespaces = getNamespaces();
        Map<String, String> attributes = getAttributes();

        for (String prefix : namespaces.keySet()) {
            combined.put(xmlNamespacePrefixToJsonFieldName(prefix), namespaces.get(prefix));
        }

        for (String name : attributes.keySet()) {
            combined.put(xmlAttributeNameToJsonFieldName(name), attributes.get(name));
        }
        
        combined.putAll(getChildren());
        
        return combined;
    }
    
    /**
     * Returns the name of the node.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the node.
     * 
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Returns the text content of the node.
     * 
     * @return the text content
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text content of the node. This has
     * no effect of text is not allowed, as determined
     * by isTextAllowed().
     * 
     * @param text the text content
     */
    public void setText(String text) {
        if (!isTextAllowed()) {
            return;
        }

        this.text = text;
    }
    
    /**
     * Adds text to the text content of the node.
     * This has no effect of text is not allowed, as
     * determined by isTextAllowed().
     * 
     * @param text the text to append
     */
    public void addText(String text) {
        if (!isTextAllowed()) {
            return;
        }
        
        this.text = this.text + text;
    }
    
    /**
     * Determines if this node contains text content.
     * 
     * @return true if the node contains text, false if text is not
     *         allowed in the node, or if the text content is empty
     */
    public boolean hasText() {
        return (isTextAllowed() && StringUtils.isNotEmpty(text));
    }

    /**
     * Returns the namespaces of the node.
     * 
     * @return a map of namespaces, where keys are namespace prefixes
     *         and values are namespace uris
     */
    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * Sets the namespaces of the node.
     * 
     * @param namespaces a map of namespaces, where keys are namespace
     *                   prefixes and values are namespace uris
     */
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }
    
    /**
     * Adds a namespace to the node.
     * 
     * @param prefix the namespace prefix
     * @param uri the namespace uri
     */
    public void addNamespace(String prefix, String uri) {
        this.namespaces.put(prefix, uri);
    }

    /**
     * Returns the attributes of the node.
     * 
     * @return a map of attributes, where keys are attribute names
     *         and values are attribute values
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Sets the attributes of the node.
     * 
     * @param attributes a map of attributes, where keys are attribute
     *                   names and values are attribute values
     */
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
    
    /**
     * Adds an attribute to the node.
     * 
     * @param name the attribute name
     * @param value the attribute value
     */
    public void addAttribute(String name, String value) {
        this.attributes.put(name, value);
    }

    /**
     * Returns the children of the node.
     * 
     * @return a map of children, where keys are node names and
     *         values are nodes or lists of nodes (where multiple
     *         child nodes have the same name)
     */
    public Map<String, Object> getChildren() {
        return children;
    }

    /**
     * Sets the children of the node.
     * 
     * @param children a map of children, where keys are node names and
     *                 values are nodes or lists of nodes (where multiple
     *                 child nodes have the same name)
     */
    public void setChildren(Map<String, Object> children) {
        this.children = children;
    }
    
    /**
     * Determines if the node has children.
     * 
     * @return true if the node has any children, false otherwise
     */
    public boolean hasChildren() {
        return this.children.size() > 0;
    }

    /**
     * <p>Adds a child node to this node.</p>
     * 
     * <p>If the node contains any text content, the text content
     * is removed, and text content is disallowed from being added
     * in the future.</p>
     * 
     * <p>If the node to be added contains no content, and
     * isRetainEmptyChildren() is false, the node is not added.</p>
     *
     * @param node the node to add as a child
     */
    public void addChild(XmlNode node) {
        // Assume mixed content is not allowed. If a child node is
        // added, text is no longer allowed, and any existing
        // text is removed.
        
        setTextAllowed(false);
        
        if (node.isEmpty() && !isRetainEmptyChildren()) {
            return;
        }
        
        Map<String, Object> children = this.getChildren();
        String name = node.getName();
        
        if (children.containsKey(name)) {
            Object existing = children.get(name);
            
            if (existing instanceof List) {
                ((List<XmlNode>) existing).add(node);
            }
            else if (existing instanceof XmlNode) {
                List<XmlNode> list = new ArrayList<XmlNode>();
                
                list.add((XmlNode) existing);
                list.add(node);
                
                children.put(name, list);
            }
        }
        else {
            children.put(name, node);
        }
    }

    /**
     * Determines if text content is allowed in this node.
     * 
     * @return true if text content is allowed, false otherwise
     */
    public boolean isTextAllowed() {
        return isTextAllowed;
    }

    /**
     * Sets whether or not text content is allowed in this node.
     * 
     * @param isTextAllowed true if text content should be allowed,
     *                      false otherwise
     */
    public void setTextAllowed(boolean isTextAllowed) {
        if (!isTextAllowed) {
            setText("");
        }
        
        this.isTextAllowed = isTextAllowed;
    }

    /**
     * Determines if empty children should be retained.
     * 
     * @return true if empty children should be retained,
     *         false otherwise
     */
    public boolean isRetainEmptyChildren() {
        return isRetainEmptyChildren;
    }

    /**
     * Sets whether or not empty children should be retained.
     * 
     * @param isRetainEmptyChildren true if empty children should be retained,
     *                              false otherwise
     */
    public void setRetainEmptyChildren(boolean isRetainEmptyChildren) {
        this.isRetainEmptyChildren = isRetainEmptyChildren;
    }
}
