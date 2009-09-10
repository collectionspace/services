/**  
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// @TODO: Add Javadoc comments

// @TODO: Need to set and enforce maximum value.
 
package org.collectionspace.services.id;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**  
 * NumericIDGeneratorPart
 *
 * Generates identifiers (IDs) that consist of a sequence of
 * numeric values, beginning from an initial value.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class NumericIDGeneratorPart implements SequenceIDGeneratorPart {
  
    final static private int DEFAULT_MAX_LENGTH = 6;
    private int maxLength = DEFAULT_MAX_LENGTH;
    
    final static private int DEFAULT_INITIAL_VALUE = 1;
    final static private long CURRENT_VALUE_NOT_SET = -1;
    private long initialValue = DEFAULT_INITIAL_VALUE;
    private long currentValue = CURRENT_VALUE_NOT_SET;

    // Constructor using defaults for initial value and maximum length.
    public NumericIDGeneratorPart() throws IllegalArgumentException {
        this(Integer.toString(DEFAULT_INITIAL_VALUE), 
            Integer.toString(DEFAULT_MAX_LENGTH));
    }

    // Constructor using default maximum length.
    public NumericIDGeneratorPart(String initialValue)
        throws IllegalArgumentException {
        this(initialValue, Integer.toString(DEFAULT_MAX_LENGTH));
    }

    // Constructor.
    public NumericIDGeneratorPart(String initialValue, String maxLength)
        throws IllegalArgumentException {

        if (initialValue == null || initialValue.equals("")) {
            throw new IllegalArgumentException(
                "Initial ID value must not be null or empty");
        }
        
        try {
            long l = Long.parseLong(initialValue.trim());
            if ( l < 0 ) {
                throw new IllegalArgumentException(
                    "Initial ID value should be zero (0) or greater");
            }
            // this.currentValue = l;
            this.initialValue = l;
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(
                "Initial ID value should not be null");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Initial ID value must be parseable as a number");
        }

        if (maxLength == null || maxLength.equals("")) {
            throw new IllegalArgumentException(
                "Initial ID value must not be null or empty");
        }

        try {
            this.maxLength = Integer.parseInt(maxLength);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Maximum ID length must be parseable as a number");
        }
        
    }

    public String getInitialID() {
        return Long.toString(this.initialValue);
    }

    public String getCurrentID() {
        if (this.currentValue == CURRENT_VALUE_NOT_SET) {
        	return Long.toString(this.initialValue);
        } else {
            return Long.toString(this.currentValue);
        }
    }

    // Sets the current value of the ID.
    public void setCurrentID(String value) throws IllegalArgumentException {

      // @TODO Much of this code is copied from the main constructor,
      // and may be ripe for refactoring.

        if (value == null || value.equals("")) {
            throw new IllegalArgumentException(
                "ID value must not be null or empty");
        }
        
        try {
            long l = Long.parseLong(value.trim());
            if ( l < 0 ) {
                throw new IllegalArgumentException(
                    "ID value should be zero (0) or greater");
            }
            this.currentValue = l;
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(
                "ID value should not be null");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "ID value must be parseable as a number");
        }
        
        // @TODO An expedient; we may need to check the String length of the
        // provided ID and calculate a maximum length here.
        this.maxLength = DEFAULT_MAX_LENGTH;
    }
    
    // Returns the next ID in the sequence, and sets the current value to that ID.
    public String nextID() throws IllegalStateException {
        if (this.currentValue == CURRENT_VALUE_NOT_SET) {
        		this.currentValue = this.initialValue;
        } else {
        	this.currentValue++;
        }
        String nextID = Long.toString(this.currentValue);
        if (nextID.length() > this.maxLength) {
            throw new IllegalStateException(
                "Next ID cannot exceed maximum length");
        }
        return nextID;
    }
 
    /**
    * Returns a new identifier.
    */
    @Override
    public String newID() {
        return nextID();
    }

    @Override
    public boolean isValidID(String id) {
    
        if (id == null) return false;
 
        // @TODO May potentially throw at least one pattern-related exception.
        // We'll need to catch and handle this here, as well as in all
        // derived classes and test cases that invoke validation.

        Pattern pattern = Pattern.compile(getRegex());
        Matcher matcher = pattern.matcher(id);
        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
        
    }

    @Override
    public String getRegex() {
        String regex =
            "(" + "\\d" + "{1," + Integer.toString(this.maxLength) + "}" + ")";
        return regex;
    }
    
}
