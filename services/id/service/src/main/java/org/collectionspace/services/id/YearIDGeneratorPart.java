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

// @TODO: Need to understand and reflect time zone issues;
// what happens when a year rollover occurs:
// - In the time zone of the end user.
// - In the time zone of the museum or other institution.
// - In the time zone of the physical server where the code is hosted.

// NOTE: This class currently hard-codes the assumption that the
// Gregorian Calendar system is in use.
//
// We may wish to use the Joda-Time framework if handling of
// additional calendar systems is needed, or additional treatment
// of time zones is warranted:
// http://joda-time.sourceforge.net/
//
// There may also be a need to have a structured set of date-time
// classes related to identifier generation.

package org.collectionspace.services.id;

import java.util.Calendar;
import java.util.GregorianCalendar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YearIDGeneratorPart
 *
 * Generates identifiers (IDs) that store and returns the current year
 * or a supplied year as a String object.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class YearIDGeneratorPart implements IDGeneratorPart {
    
	private String initialValue = null;
	private String currentValue = null;
	
    // NOTE: Currently hard-coded to accept only a range of
    // four-digit Gregorian Calendar year dates.
    final static String REGEX_PATTERN = "(\\d{4})";

	public YearIDGeneratorPart() throws IllegalArgumentException {

		String currentYear = getCurrentYear();
		this.initialValue = currentYear;
		this.currentValue = currentYear;

	}
	
	public YearIDGeneratorPart(String initialValue)
	    throws IllegalArgumentException {

		if (initialValue == null || initialValue.equals("")) {
			throw new IllegalArgumentException(
			    "Initial ID value must not be null or empty");
		}
		
		// @TODO: Add regex-based validation here, by calling isValidID().
		// Consider implications for Internationalization when doing so.
		
		this.initialValue = initialValue;
		this.currentValue = initialValue;

	}

	public String getInitialID() {
		return this.initialValue;
	}

	public String getCurrentID() {
		return this.currentValue;
	}

  // Sets the current value.
	public void setCurrentID(String value) throws IllegalArgumentException {

	  // @TODO This code is copied from the main constructor,
	  // and thus there may be an opportunity for refactoring.

		if (value == null || value.equals("")) {
			throw new IllegalArgumentException("ID value must not be null or empty");
		}
		
		// @TODO: Add regex-based validation here, by calling isValidID().
		// Consider implications for Internationalization when doing so.

		this.currentValue = value;

	}
	
	public void resetID() {
		this.currentValue = this.initialValue;
	}

	// @TODO: We'll need to decide what a "next" ID means in the context of:
	// - An initially supplied value.
	// - A year value that has not changed from its previous value.
	// - A year value that has changed, as a result of a rollover
	//   to a new instant in time.
	public String nextID() {
		return this.currentValue;
    }
    
    @Override
    public String newID() {
        return getCurrentYear();
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
		return REGEX_PATTERN;
	}

	public static String getCurrentYear() {
		Calendar cal = GregorianCalendar.getInstance();
        int y = cal.get(Calendar.YEAR);
		return Integer.toString(y);
	}	
	
}
