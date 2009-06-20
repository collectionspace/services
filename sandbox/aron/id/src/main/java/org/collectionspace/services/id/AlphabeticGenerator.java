 /*	
 * AlphabeticGenerator
 *
 * <p>An identifier generator that generates an incrementing ID from any composite
 * of the USASCII character sequences 'A-Z' and 'a-z', as a String object.</p>
 *
 * <p>The <code>wrap</code> property determines whether or not the sequence wraps
 * when it reaches the largest value that can be represented in <code>size</code>.
 * If <code>wrap</code> is false and the the maximum representable
 * value is exceeded, an IllegalStateException is thrown</p>
 *
 * Copyright 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * @author $Author$
 * @version $Revision$
 * $Date$
 */
 
// @TODO: The initial value determines the fixed number of characters.
// We may also need to model cases where the number of characters
// increases as values roll over, up to a specified maximum number of
// characters; e.g. "z" becomes "aa", and "ZZ" becomes "AAA".

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
 
package org.collectionspace.services.id;

import org.apache.commons.id.AbstractStringIdentifierGenerator;
import java.io.Serializable;

public class AlphabeticGenerator extends AbstractStringIdentifierGenerator
	implements Serializable {

    /**
     * <code>serialVersionUID</code> is the serializable UID for the binary version of the class.
     */
    // private static final long serialVersionUID = 20060120L;  // @TODO ReplaceMe!

    /**
     * Should the counter wrap.
     */
    private boolean wrapping = true;

    /**
     * The counter.
     */
    private char[] count = null;
    private char[] initialcount = null;

    /**
     * 'Z' and 'z' chars
     */
    private static final char LOWERCASE_Z_CHAR = 'z';
    private static final char UPPERCASE_Z_CHAR = 'Z';

    /**
     * Constructor with a default size for the alphanumeric identifier.
     *
     * @param wrap should the factory wrap when it reaches the maximum
     *   value (or throw an exception)
     */
    public AlphabeticGenerator(boolean wrap) {
        this(wrap, DEFAULT_ALPHANUMERIC_IDENTIFIER_SIZE);
    }

    /**
     * Constructor.
     *
     * @param wrap should the factory wrap when it reaches the maximum
     *   value (or throw an exception)
     * @param size  the size of the identifier
     */
    public AlphabeticGenerator(boolean wrap, int size) {
        super();
        this.wrapping = wrap;
        if (size < 1) {
            throw new IllegalArgumentException("The size must be at least one");
        }
        this.count = new char[size];
        
        // Initialize the contents of the identifier's character array
        for (int i = 0; i < size; i++) {
            count[i] = ' ';  // space
        }
    }

    /**
     * Construct with a counter, that will start at the specified
     * alphanumeric value.</p>
     *
     * @param wrap should the factory wrap when it reaches the maximum
     * value (or throw an exception)
     * @param initialValue the initial value to start at
     */
    public AlphabeticGenerator(boolean wrap, String initialValue) {
        super();
        this.wrapping = wrap;
 
 				if ( initialValue == null ) {
					throw new IllegalArgumentException("Initial value must not be null");
				}

 				if ( initialValue == "" ) {
					throw new IllegalArgumentException("Initial value must not be empty");
				}

       this.count = initialValue.toCharArray();

        // Validate each of the characters in the initial value
        // against ranges of valid values.
        for (int i = 0; i < this.count.length; i++) {
            char ch = this.count[i];
            if (ch >= 'A' && ch <= 'Z') continue;
            if (ch >= 'a' && ch <= 'z') continue;
            
            throw new IllegalArgumentException(
                    "character " + this.count[i] + " is not valid");
        }
        
        // Store the initial character array
        this.initialcount = this.count;
    }
    
    public long maxLength() {
        return this.count.length;
    }

    public long minLength() {
        return this.count.length;
    }

    /**
     * Getter for property wrap.
     *
     * @return <code>true</code> if this generator is set up to wrap.
     *
     */
    public boolean isWrap() {
        return wrapping;
    }

    /**
     * Sets the wrap property.
     *
     * @param wrap value for the wrap property
     *
     */
    public void setWrap(boolean wrap) {
        this.wrapping = wrap;
    }

    /**
     * Returns the (constant) size of the strings generated by this generator.
     *
     * @return the size of generated identifiers
     */
    public int getSize() {
        return this.count.length;
    }

    public synchronized String nextStringIdentifier() {
        
        // Get next values for each character from right to left
        for (int i = count.length - 1; i >= 0; i--) {
            switch (count[i]) {
            
                case LOWERCASE_Z_CHAR:  // z
                    if (i == 0 && !wrapping) {
                        throw new IllegalStateException
                        ("The maximum number of identifiers has been reached");
                    }
                    count[i] = 'a';
                    break;

               case UPPERCASE_Z_CHAR:  // Z
                    if (i == 0 && !wrapping) {
                        throw new IllegalStateException
                        ("The maximum number of identifiers has been reached");
                    }
                    count[i] = 'A';
                    break;

                default:
                    count[i]++;
                    i = -1;
                    break;
            }
        }
        return new String(count);
    }
}
