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

    private IDPartOutputFormatter formatter = new NumericIDPartOutputFormatter();
    private IDPartValidator validator = new NumericIDPartRegexValidator();

    public NumericSequenceIDPart() {
    }

    public NumericSequenceIDPart(String formatPattern) {
        setOutputFormatter(new NumericIDPartOutputFormatter(formatPattern));
    }

    public NumericSequenceIDPart(long initial) {
        setInitialID(initial);
    }

    public NumericSequenceIDPart(long initial, long incrementBy) {
        setInitialID(initial);
        setIncrementBy(incrementBy);
    }

    public NumericSequenceIDPart(String formatPattern, long initial,
        long incrementBy) {
        setOutputFormatter(new NumericIDPartOutputFormatter(formatPattern));
        setInitialID(initial);
        setIncrementBy(incrementBy);
    }

    @Override
    public IDPartOutputFormatter getOutputFormatter() {
        return this.formatter;
    }

    public void setOutputFormatter(IDPartOutputFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public IDPartValidator getValidator() {
        return this.validator;
    }

    @Override
    public String newID() {
        String newID = super.newID();
        return getOutputFormatter().format(newID);
    }

    @Override
    public boolean hasCurrentID() {
        return (this.currentValue == CURRENT_VALUE_NOT_SET) ? false : true;
    }

    @Override
    public String getCurrentID() {
        return Long.toString(this.currentValue);
    }

    // @TODO Consider throwing IllegalArgumentException here.
    public void setCurrentID(long val) {
        if (val < 0) {
            logger.error("Current ID value for numeric ID sequences " +
                "must be positive.");
        } else {
            this.currentValue = val;
        }
    }

    @Override
    public void setCurrentID(String id) {
        String formatPattern = getOutputFormatter().getFormatPattern();
        if (formatPattern == null || formatPattern.trim().isEmpty()) {
            try {
                setCurrentID(Long.parseLong(id));
            } catch (NumberFormatException e) {
                logger.error("Could not parse current ID value as a number.", e);
            }
        } else {
            setCurrentID(((NumericIDPartOutputFormatter)
                getOutputFormatter()).parseAsLong(id));
        }
    }

    @Override
    public String getInitialID() {
        return Long.toString(this.initialValue);
    }

    // @TODO Consider throwing IllegalArgumentException here.
    public void setInitialID(long initial) {
        if (initial < 0) {
            logger.error("Current ID value for numeric ID sequences " +
                "must be positive.");
        } else {
            this.initialValue = initial;
        }
    }

    @Override
    public String nextID() {
        return Long.toString(this.currentValue + this.incrementBy);
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

