package org.collectionspace.services.id.part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumericSequenceIDPart extends SequenceIDPart {

    final Logger logger =
        LoggerFactory.getLogger(NumericSequenceIDPart.class);

    final static private long DEFAULT_INITIAL_VALUE = 1;
    private long initialValue = DEFAULT_INITIAL_VALUE;

    final static private long CURRENT_VALUE_NOT_SET = -1;
    private long currentValue = CURRENT_VALUE_NOT_SET;

    final static private long DEFAULT_INCREMENT_BY_VALUE = 1;
    private long incrementBy = DEFAULT_INCREMENT_BY_VALUE;

    // @TODO Replace the NoOp formatter with a printf formatter.
    private IDPartOutputFormatter formatter = new NoOpIDPartOutputFormatter();
    private IDPartValidator validator = new NumericIDPartRegexValidator();


    public NumericSequenceIDPart() {
    }

    public NumericSequenceIDPart(long initial) {
        setInitialID(initial);
    }

    public NumericSequenceIDPart(long initial, long incrementBy) {
        setInitialID(initial);
        setIncrementBy(incrementBy);
    }

    @Override
    public IDPartOutputFormatter getOutputFormatter() {
        return this.formatter;
    }

    public void setOutputFormatter (IDPartOutputFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public IDPartValidator getValidator() {
        return this.validator;
    }

    // newID() is implemented in superclass, SequenceIDPart.

    @Override
    public boolean hasCurrentID() {
        return (this.currentValue == CURRENT_VALUE_NOT_SET) ? false : true;
    }

    @Override
    public String getCurrentID() {
        return Long.toString(this.currentValue);
    }

    public void setCurrentID(long val) {
        if (val <= 0) {
            logger.error("Current ID value for numeric ID sequences " +
                "must be positive.");
        } else {
            this.currentValue = val;
        }
    }

    @Override
    public void setCurrentID(String str) {
        try {
            setCurrentID(Long.parseLong(str));
        } catch (NumberFormatException e) {
            logger.error("Could not parse current ID value as a number.", e);
        }
    }

    @Override
    public String getInitialID() {
        return Long.toString(this.initialValue);
    }

    public void setInitialID(long initial) {
        if (initial <= 0) {
            logger.error("Current ID value for numeric ID sequences " +
                "must be positive.");
        } else {
            this.initialValue = initial;
        }
    }

    @Override
    public String nextID() {
        // @TODO Rethink this approach soon, as we may not want
        // to change the current value for IDs that have been
        // provisionally issued.
        this.currentValue = this.currentValue + this.incrementBy;
        return getCurrentID();
    }

    public String getIncrementBy() {
        return Long.toString(this.incrementBy);
    }

    private void setIncrementBy(long incrementBy) {
        if (incrementBy <= 0) {
            logger.error("Increment-by value for numeric ID sequences " +
                "must be positive.");
        } else {
            this.incrementBy = incrementBy;
        }
    }

}

