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
// classes related to identifier generation, which can also be
// facilitated through the use of Joda-Time.

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
    
	private String currentValue = null;
	
    // NOTE: Currently hard-coded to accept only a range of
    // four-digit Gregorian Calendar year dates.
    final static String REGEX_PATTERN = "(\\d{4})";

	public YearIDGeneratorPart() throws IllegalArgumentException {
		this.currentValue = getCurrentYear();
	}
	
	public YearIDGeneratorPart(String yearValue)
	    throws IllegalArgumentException {
        setCurrentID(yearValue);
	}

    @Override
	public void setCurrentID(String yearValue) throws IllegalArgumentException {
		if (yearValue == null || yearValue.equals("")) {
			throw new IllegalArgumentException(
			    "Supplied year must not be null or empty");
		}
		// NOTE: Validation currently is based on a
		// hard coded year format and calendar system.
		if (! isValidID(yearValue)) {
			throw new IllegalArgumentException(
			    "Supplied year '" + yearValue + "' is not valid.");
		}
		this.currentValue = yearValue;
	}

    @Override
	public String getCurrentID() {
		return this.currentValue;
	}

    @Override
    public String newID() {
        return this.currentValue;
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
		return REGEX_PATTERN;
	}

    // NOTE: Currently hard-coded to use the Gregorian Calendar system.
	public static String getCurrentYear() {
		Calendar cal = GregorianCalendar.getInstance();
        int year = cal.get(Calendar.YEAR);
		return Integer.toString(year);
	}	
	
}
