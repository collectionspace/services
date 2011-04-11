
package org.collectionspace.services.id.part;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Returns a evenly-distributed, pseudorandom number from within a
// default or supplied range of integer values.

public class JavaRandomNumberIDPartAlgorithm implements IDPartAlgorithm {

    // @TODO Verify whether this simple singleton pattern is
    // achieving the goal of using a single instance of the random
    // number generator.

    // @TODO Check whether we might need to store a serialization
    // of this class, once instantiated, between invocations, and
    // load the class from its serialized state, to reduce the
    // possibility of

    // @TODO Look into whether we may have some user stories or use cases
    // that require the use of java.security.SecureRandom, rather than
    // java.util.Random.

    final Logger logger =
        LoggerFactory.getLogger(JavaRandomNumberIDPartAlgorithm.class);

    // Starting with Java 5, the default instantiation of Random()
    // sets the seed "to a value very likely to be distinct from any
    // other invocation of this constructor."
    private static Random r = new Random();

    public final static int DEFAULT_MAX_VALUE = Integer.MAX_VALUE - 2;
    public final static int DEFAULT_MIN_VALUE = 0;

    private int maxValue = DEFAULT_MAX_VALUE;
    private int minValue = DEFAULT_MIN_VALUE;

    public JavaRandomNumberIDPartAlgorithm() {
    }

    // Throws IllegalArgumentException
    public JavaRandomNumberIDPartAlgorithm(int maxVal) {
        try {
            setMaxValue(maxVal);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    // Throws IllegalArgumentException
    public JavaRandomNumberIDPartAlgorithm(int maxVal, int minVal) {
        setMaxValue(maxVal);
        setMinValue(minVal);
    }

    private void setMaxValue(int maxVal) {
        if (0 < maxVal && maxVal <= DEFAULT_MAX_VALUE) {
            this.maxValue = maxVal;
        } else {
            String msg =
                "Invalid maximum value '" +
                Integer.toString(maxVal) +
                "' for random number. " +
                "Must be between 1 and " +
                Integer.toString(DEFAULT_MAX_VALUE) +
                ", inclusive.";
            logger.info(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    private void setMinValue(int minVal) {
        if (DEFAULT_MIN_VALUE <= minVal && minVal < this.maxValue) {
            this.minValue = minVal;
        } else {
            String msg =
                "Invalid minimum value '" +
                Integer.toString(minVal) +
                "' for random number. " +
                "Must be between 0 and " +
                Integer.toString(this.maxValue - 1) + 
                ", inclusive (i.e. less than the supplied maximum value " +
                "of " + Integer.toString(this.maxValue) + ").";
            logger.info(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public String generateID(){
        // Returns an evenly distributed random value between 0
        // and the maximum value.  An even distribution decreases
        // randomness but is more likely to meet end user requirements
        // and expectations.
        //
        // See http://mindprod.com/jgloss/pseudorandom.html
        //
        // Note: Random.nextInt() returns a pseudorandom number
        // between 0 and n-1 inclusive, not a number between 0 and n.

        // @TODO Consider adding code to ensure the uniqueness of
        // each generated pseudorandom number, until all possible
        // values within the inclusive set have been generated.
        return
            Integer.toString(r.nextInt(
            this.maxValue - this.minValue + 1) + this.minValue);

    }

}
