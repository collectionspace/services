package org.collectionspace.services.id.part;

public abstract class DateIDPart implements IDPart, DynamicValueIDPart {

    public DateIDPart () {
    }

    @Override
    public abstract IDPartOutputFormatter getOutputFormatter();

    @Override
    public abstract IDPartValidator getValidator();

    @Override
    public abstract String newID();

}

