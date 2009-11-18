package org.collectionspace.services.id.part;

// @TODO Largely unimplemented at present.
// Corresponding test class has not yet been created.

public class AlphabeticSequenceIDPart extends SequenceIDPart {

    // @TODO Externalize character sequences to their own class.
    private AlphabeticSequenceIDPart.AlphabeticCharSequence charsInSequence;
    private IDPartOutputFormatter formatter;
    private IDPartValidator validator;
    private char initialValue;

    public AlphabeticSequenceIDPart () {
    }

    @Override
    public IDPartOutputFormatter getOutputFormatter () {
        return formatter;
    }

    public void setOutputFormatter (IDPartOutputFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public IDPartValidator getValidator() {
        return this.validator;
    }

    @Override
    public boolean hasCurrentID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getCurrentID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setCurrentID(String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getInitialID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String nextID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public char getInitialValue () {
        return initialValue;
    }

    public void setInitialValue (char val) {
        this.initialValue = val;
    }

    public AlphabeticSequenceIDPart.AlphabeticCharSequence getCharsInSequence () {
        return charsInSequence;
    }

    public void setCharsInSequence
        (AlphabeticSequenceIDPart.AlphabeticCharSequence val) {
        this.charsInSequence = val;
    }

    public enum AlphabeticCharSequence {
        ;
    }

}

