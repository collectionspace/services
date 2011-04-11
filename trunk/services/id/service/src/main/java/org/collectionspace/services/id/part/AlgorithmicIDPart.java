package org.collectionspace.services.id.part;

public abstract class AlgorithmicIDPart implements IDPart, DynamicValueIDPart {

    @Override
    public abstract IDPartOutputFormatter getOutputFormatter();

    @Override
    public abstract IDPartValidator getValidator();

    public abstract IDPartAlgorithm getAlgorithm();

    @Override
    public String newID() {
        return getOutputFormatter().format(getAlgorithm().generateID());
    }

}

