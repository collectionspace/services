package org.collectionspace.services.id.part;

public class StaticValueIDPart implements IDPart {

    private IDPartOutputFormatter formatter;
    private IDPartValidator validator;
    private String initialValue;

    public StaticValueIDPart() {
    }

    @Override
    public String newID() {
        return getInitialValue();
    }

    @Override
    public IDPartOutputFormatter getOutputFormatter() {
        return this.formatter;
    }

    @Override
    public IDPartValidator getValidator() {
        return this.validator;
    }

    public String getInitialValue() {
        return this.initialValue;
    }

    public void setInitialValue(String val) {
        this.initialValue = val;
    }

}

