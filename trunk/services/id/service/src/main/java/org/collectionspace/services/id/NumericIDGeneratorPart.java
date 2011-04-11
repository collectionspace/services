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
 
// @TODO Need to handle Exceptions as described in comments below.

// @TODO Need to add optional capability to pad with leading zeros.
 
package org.collectionspace.services.id;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.collectionspace.services.common.document.BadRequestException;

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
  
    final static private long DEFAULT_MAX_LENGTH = 6;
    private long maxLength = DEFAULT_MAX_LENGTH;
    
    final static private long DEFAULT_INITIAL_VALUE = 1;
    final static private long CURRENT_VALUE_NOT_SET = -1;
    private long initialValue = DEFAULT_INITIAL_VALUE;
    private long currentValue = CURRENT_VALUE_NOT_SET;

    /**
     * Constructor using defaults for initial value and maximum length.
     */
    public NumericIDGeneratorPart() throws BadRequestException {
        this(Long.toString(DEFAULT_INITIAL_VALUE),
            Long.toString(DEFAULT_MAX_LENGTH));
    }

    /**
     * Constructor using defaults for initial value and maximum length.
     *
     * @param initialValue  The initial value of the numeric ID.
     */
    public NumericIDGeneratorPart(String initialValue)
        throws BadRequestException {
        this(initialValue, Long.toString(DEFAULT_MAX_LENGTH));
    }

    /**
     * Constructor.
     *
     * @param initialValue  The initial value of the numeric ID.
     *
     * @param maxLength  The maximum String length for generated IDs.
     */
    public NumericIDGeneratorPart(String initialValue, String maxLength)
        throws BadRequestException {

        if (maxLength == null || maxLength.equals("")) {
            throw new BadRequestException(
                "Initial ID value must not be null or empty");
        }
        try {
            this.maxLength = Long.parseLong(maxLength);
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                "Maximum ID length must be parseable as a number");
        }
        if (initialValue == null || initialValue.equals("")) {
            throw new BadRequestException(
                "Initial ID value must not be null or empty");
        }
        
        try {
            long initVal = Long.parseLong(initialValue.trim());
            if ( initVal < 0 ) {
                throw new BadRequestException(
                    "Initial ID value should be zero (0) or greater");
            }
            String initValStr = Long.toString(initVal);
            if (initValStr.length() > this.maxLength) {
                throw new IllegalStateException(
                    "Initial ID cannot exceed maximum length of " +
                    maxLength + ".");
            }
            this.initialValue = initVal;
        } catch (NullPointerException e) {
            throw new BadRequestException(
                "Initial ID value should not be null");
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                "Initial ID value must be parseable as a number");
        }

    }

    @Override
    public String getInitialID() {
        return Long.toString(this.initialValue);
    }

    @Override
    public void setCurrentID(String value) throws BadRequestException {

      // @TODO Much of this code is copied from the main constructor,
      // and may be ripe for refactoring.

        if (value == null || value.equals("")) {
            throw new BadRequestException(
                "ID value must not be null or empty");
        }
        
        try {
            long currVal = Long.parseLong(value.trim());
            if ( currVal < 0 ) {
                throw new BadRequestException(
                    "ID value should be zero (0) or greater");
            }
            String currValStr = Long.toString(currVal);
            if (currValStr.length() > this.maxLength) {
                throw new IllegalStateException(
                    "Current ID cannot exceed maximum length of " +
                    maxLength + ".");
            }
            this.currentValue = currVal;
        } catch (NullPointerException e) {
            throw new BadRequestException(
                "ID value should not be null");
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                "ID value must be parseable as a number");
        }
    }
    

    @Override
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

    @Override
    public String getCurrentID() {
        if (this.currentValue == CURRENT_VALUE_NOT_SET) {
        	return Long.toString(this.initialValue);
        } else {
            return Long.toString(this.currentValue);
        }
    }
    
    // @TODO Need to handle exceptions thrown by nextID();
    // currently, newID simply throws an uncaught and unreported exception.

    @Override
    public String newID() {
        return nextID();
    }

    @Override
    public boolean isValidID(String id) {
    
        if (id == null) {
            return false;
        }
 
        // @TODO May potentially throw java.util.regex.PatternSyntaxException.
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
            "(" + "\\d" + "{1," + Long.toString(this.maxLength) + "}" + ")";
        return regex;
    }
    
}
