package org.collectionspace.services.id.part;

public abstract class SequenceIDPart implements IDPart, DynamicValueIDPart {

    public SequenceIDPart () {
    }

    @Override
    public abstract IDPartOutputFormatter getOutputFormatter();

    @Override
    public abstract IDPartValidator getValidator();

    @Override
    public String newID() {
        if (hasCurrentID()) {
            return nextID();
        } else {
            return getInitialID();
        }
    }

    abstract public boolean hasCurrentID();

    abstract public String getCurrentID();

    abstract public void setCurrentID(String s);

    abstract public String getInitialID();

    abstract public String nextID();

}

