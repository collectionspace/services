package org.collectionspace.services.id.part;

public interface IDPart {

    IDPartOutputFormatter DEFAULT_FORMATTER = new NoOpIDPartOutputFormatter();
    IDPartValidator DEFAULT_VALIDATOR = new NoOpIDPartValidator();

    public IDPartOutputFormatter getOutputFormatter();

    public IDPartValidator getValidator();

    public String newID();

}

