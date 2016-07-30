package org.collectionspace.services.common.xmljson.parsetree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 
 */
public class Node {
    private String name;
    private String text = "";
    private Map<String, String> namespaces = new LinkedHashMap<String, String>();
    private Map<String, String> attributes = new LinkedHashMap<String, String>();
    private Map<String, Object> children = new LinkedHashMap<String, Object>();
    private boolean isTextAllowed = true;
    
    public static String hashKeyForAttributeName(String name) {
        return "@" + name;
    }

    public static String hashKeyForNamespacePrefix(String prefix) {
        return hashKeyForAttributeName("xmlns:" + prefix);
    }
    
    public Node() {
        
    }

    public Node(String name) {
        setName(name);
    }
    
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
    
    public boolean isEmpty() {
        return (!(hasChildren() || hasText()));
    }
    
    public Map<String, Object> getCombinedMap() {
        Map<String, Object> combined = new LinkedHashMap<String, Object>();
        Map<String, String> namespaces = getNamespaces();
        Map<String, String> attributes = getAttributes();

        for (String prefix : namespaces.keySet()) {
            combined.put(hashKeyForNamespacePrefix(prefix), namespaces.get(prefix));
        }

        for (String name : attributes.keySet()) {
            combined.put(hashKeyForAttributeName(name), attributes.get(name));
        }
        
        combined.putAll(getChildren());
        
        return combined;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (!isTextAllowed()) {
            return;
        }

        this.text = text;
    }
    
    public void addText(String text) {
        if (!isTextAllowed()) {
            return;
        }
        
        this.text = this.text + text;
    }
    
    public boolean hasText() {
        return (isTextAllowed() && StringUtils.isNotEmpty(text));
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }
    
    public void addNamespace(String prefix, String uri) {
        this.namespaces.put(prefix, uri);
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
    
    public void addAttribute(String name, String value) {
        this.attributes.put(name, value);
    }

    public Map<String, Object> getChildren() {
        return children;
    }

    public void setChildren(Map<String, Object> children) {
        this.children = children;
    }
    
    public boolean hasChildren() {
        return this.children.size() > 0;
    }

    public void addChild(Node node) {
        // Assume mixed content is not allowed. If a child node is
        // added, text is no longer allowed, and any existing
        // text is removed.
        
        if (isTextAllowed()) {
            setText("");
            setTextAllowed(false);
        }
        
        if (node.isEmpty()) {
            return;
        }
        
        Map<String, Object> children = this.getChildren();
        String name = node.getName();
        
        if (children.containsKey(name)) {
            Object existing = children.get(name);
            
            if (existing instanceof List) {
                ((List<Node>) existing).add(node);
            }
            else if (existing instanceof Node) {
                List<Node> list = new ArrayList<Node>();
                
                list.add((Node) existing);
                list.add(node);
                
                children.put(name, list);
            }
        }
        else {
            children.put(name, node);
        }
    }

    public boolean isTextAllowed() {
        return isTextAllowed;
    }

    public void setTextAllowed(boolean isTextAllowed) {
        this.isTextAllowed = isTextAllowed;
    }
}
