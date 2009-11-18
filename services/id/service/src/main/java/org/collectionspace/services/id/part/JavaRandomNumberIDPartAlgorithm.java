
package org.collectionspace.services.id.part;

import java.util.Random;

public class JavaRandomNumberIDPartAlgorithm implements IDPartAlgorithm {

    // @TODO Verify whether this simple singleton pattern is
    // achieving the goal of using a single instance of the random
    // number generator.

    // @TODO Check whether we might need to store a serialization
    // of this class, once instantiated, between invocations, and
    // load the class from its serialized state.

    // @TODO Look into whether we may have some user stories or use cases
    // that require the use of java.security.SecureRandom, rather than
    // java.util.Random.

    // Starting with Java 5, the default instantiation of Random()
    // sets the seed "to a value very likely to be distinct from any
    // other invocation of this constructor."
    private Random r = new Random();

    private JavaRandomNumberIDPartAlgorithm() {
    }

    // See http://en.wikipedia.org/wiki/Singleton_pattern
    private static class SingletonHolder {
        private static final JavaRandomNumberIDPartAlgorithm INSTANCE =
            new JavaRandomNumberIDPartAlgorithm();
    }

    public static JavaRandomNumberIDPartAlgorithm getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // @TODO Allow setting a maximum value.
    @Override
    public String generateID(){
        // Returns a value between 0 (inclusive) and the
        // maximum value of an int.
        return Integer.toString(r.nextInt(Integer.MAX_VALUE));
    }

}
