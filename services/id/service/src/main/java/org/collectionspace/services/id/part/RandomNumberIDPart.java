package org.collectionspace.services.id.part;

public class RandomNumberIDPart extends AlgorithmicIDPart {

    // @TODO The name of this class may be too generic, since there
    // may be other random number generators with different algorithms.

    private IDPartOutputFormatter formatter = IDPart.DEFAULT_FORMATTER;
    private IDPartValidator validator = new NumericIDPartRegexValidator();
    private IDPartAlgorithm algorithm =
        JavaRandomNumberIDPartAlgorithm.getInstance();

    public RandomNumberIDPart(){
    }

    @Override
    public IDPartOutputFormatter getOutputFormatter() {
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

