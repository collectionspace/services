package org.collectionspace.services.id.part;

import java.util.ArrayList;
import java.util.LinkedHashSet;

// @TODO Largely unimplemented at present.  Much code is non-working.

public class AlphabeticSequenceIDPart extends SequenceIDPart {

    private IDPartOutputFormatter formatter = new NoOpIDPartOutputFormatter();
    private IDPartValidator validator = new NoOpIDPartValidator();

    // @TODO Externalize character sequences to their own class.
    private AlphabeticSequenceIDPart.AlphabeticCharSequence charsInSequence;

    LinkedHashSet<Character> alphabeticSequence = new LinkedHashSet<Character>();
    private ArrayList<Character> initialValue = new ArrayList<Character>();
    private ArrayList<Character> currentValue = new ArrayList<Character>();

    private static final char NULL_CHAR = '\u0000';

    private char startChar = NULL_CHAR;
    private char endChar = NULL_CHAR;

    public AlphabeticSequenceIDPart () {
    }

    public AlphabeticSequenceIDPart(LinkedHashSet<Character> sequence) {
        this.alphabeticSequence = sequence;
        Character[] chars = (Character[]) alphabeticSequence.toArray();
        this.startChar = chars[0].charValue();
        // initialValue.add(new Character(start.char));
        this.endChar = chars[chars.length - 1].charValue();
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

        // Get next values for each character, from right to left
        // (least significant to most significant).
        boolean expandIdentifier = false;
        int size = this.currentValue.size();
        char ch;
        for (int i = (size - 1); i >= 0; i--) {

            ch = this.currentValue.get(i).charValue();

            // When we reach the maximum value for any character,
            // 'roll over' to the minimum value in our character range.
            if (ch == this.endChar) {
                this.currentValue.set(i, Character.valueOf(this.startChar));
                // If this rollover occurs in the most significant value,
                // set a flag to later expand the size of the identifier.
                //
                // @TODO: Set another flag to enable or disable this behavior,
                // as well as a mechanism for setting the maximum expansion
                // permitted.
                if (i == 0) {
                  expandIdentifier = true;
                }
            // When we reach the most significant character whose value
            // doesn't roll over, increment that character and exit the loop.
            } else {
                ch++;
                this.currentValue.set(i, Character.valueOf(ch));
                i = -1;
                break;
            }

        }

        // If we are expanding the size of the identifier, insert a new
        // value at the most significant (leftmost) character position,
        // sliding other values to the right.
        if (expandIdentifier) {
            this.currentValue.add(0, Character.valueOf(this.startChar));
        }

        return toIDString(this.currentValue);

    }

    public char getInitialValue () {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setInitialValue (char val) {
        throw new UnsupportedOperationException("Not supported yet.");
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

    public String toIDString(ArrayList<Character> characters) {
        StringBuffer sb = new StringBuffer();
        for ( Character ch : characters ) {
            sb.append(ch.toString());
        }
        return sb.toString();
    }
}

