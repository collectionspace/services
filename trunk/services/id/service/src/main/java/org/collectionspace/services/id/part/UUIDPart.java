package org.collectionspace.services.id.part;

public class UUIDPart extends AlgorithmicIDPart {

    // @TODO The name of this class may be too generic, since
    // there may be other UUID generators with different algorithms.

    private IDPartOutputFormatter formatter = IDPart.DEFAULT_FORMATTER;
    private IDPartValidator validator = new UUIDType4PartRegexValidator();
    private IDPartAlgorithm algorithm = new UUIDType4IDPartAlgorithm();

    public UUIDPart() {
    }

    @Override
    public IDPartOutputFormatter getOutputFormatter () {
        return this.formatter;
    }

    @Override
    public IDPartValidator getValidator() {
        return this.validator;
    }

    @Override
    public IDPartAlgorithm getAlgorithm() {
        return this.algorithm;
    }

    // newID() is implemented in superclass, AlgorithmicIDPart.

}

