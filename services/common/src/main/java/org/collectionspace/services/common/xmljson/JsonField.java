package org.collectionspace.services.common.xmljson;

/**
 * A lightweight representation of a JSON field. Instances are created
 * by JsonToXmlStreamConverter in the course of parsing JSON, in order
 * to track the current state.
 * 
 * Each JSON field has a name and a type, which is either scalar, array,
 * or object.
 */
public class JsonField {
    private String name;
    private Type type;
    
    /**
     * Creates an unnamed JsonField.
     */
    public JsonField() {
        
    }
    
    /**
     * Creates a JsonField with a given name, and scalar type.
     * 
     * @param name the name
     */
    public JsonField(String name) {
        this.setName(name);
        this.setType(Type.SCALAR);
    }

    /**
     * Returns the name of the field.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the field.
     * 
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the type of the field.
     * 
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of the field.
     * 
     * @param type the type
     */
    public void setType(Type type) {
        this.type = type;
    }
    
    /**
     * Determines if this is a scalar field.
     * 
     * @return true if this is a scalar field, false otherwise
     */
    public boolean isScalar() {
        return (getType() == Type.SCALAR);
    }
    
    /**
     * Determines if this is an array field.
     * 
     * @return true if this is an array field, false otherwise
     */
    public boolean isArray() {
        return (getType() == Type.ARRAY);
    }
    
    /**
     * The possible field types.
     */
    public enum Type {
        SCALAR,
        ARRAY,
        OBJECT
    }
}