/*	
 * AlphabeticIDGenerator
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
 * @author $Author: aron $
 * @version $Revision: 267 $
 * $Date: 2009-06-19 19:03:38 -0700 (Fri, 19 Jun 2009) $
 */

// @TODO: Add Javadoc comments
 
// @TODO: The initial value determines the fixed number of characters.
// We may also need to model cases where the number of characters
// increases as values roll over, up to a specified maximum number of
// characters; e.g. "z" becomes "aa", and "ZZ" becomes "AAA". When
// doing so, we'll also need to set a maximum length to which the
// generated IDs can grow.

// @TODO: This class is hard-coded to use two series within the
// USASCII character set.
//
// With some minor refactoring, we could draw upon minimum and maximum
// character values for a wide range of arbitrary character sets.

// Some code and algorithms in the current iteration of this class
// were adapted from the org.apache.commons.Id package, and thus
// the relevant licensing terms are included here:

/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
 
package org.collectionspace.services.id;

public class AlphabeticIDGenerator implements IDGenerator {
    
	private static final char LOWERCASE_Z_CHAR = 'z';
	private static final char UPPERCASE_Z_CHAR = 'Z';

	private char[] initialValue = null;
	private char[] currentValue = null;
	
	public AlphabeticIDGenerator(String initialValue) throws IllegalArgumentException {

		if ( initialValue == null ) {
			throw new IllegalArgumentException("Initial value must not be null");
		}
	
		if ( initialValue == "" ) {
			throw new IllegalArgumentException("Initial value must not be empty");
		}
	
		char[] charsToValidate = initialValue.toCharArray();
		
		// Validate each of the characters in the initial value
		// against ranges of valid values.
		for (int i = 0; i < charsToValidate.length; i++) {
		
			char ch = charsToValidate[i];
			
			// If the value of the current character matches a character
			// in the uppercase ('A-Z') or lowercase ('a-z') series
			// in the USASCII character set, that character has a valid value,
			// so we can skip to checking the next character.
			if (ch >= 'A' && ch <= 'Z') continue;
			if (ch >= 'a' && ch <= 'z') continue;
			
			// Otherwise, we've detected a character not in those series.
			throw new IllegalArgumentException(
				"character " + charsToValidate[i] + " is not valid");
				
		} // end 'for' loop
		
		// Store the initial character array
		this.initialValue = charsToValidate;
		this.currentValue = charsToValidate;

	}

	public synchronized void reset() {
		try {
			// TODO: Investigate using different methods to perform this copying,
			// such as clone.  See "Java Practices - Copy an Array"
			// <http://www.javapractices.com/topic/TopicAction.do?Id=3>
			// char [] copy = (char []) initialValue.clone();
			// this.currentValue = copy;
			// System.arraycopy( 
			//	this.initialValue, 0, this.currentValue, 0, this.initialValue.length );
      for ( int i = 0; i < this.initialValue.length; ++i ) {
        this.currentValue[i] = this.initialValue[i];
      }
		// If copying would cause access of data outside array bounds.
		} catch (IndexOutOfBoundsException iobe) {
			// For experimentation - do nothing here at this time.
		// If an element in the source array could not be stored into
		// the destination array because of a type mismatch. 
		} catch (ArrayStoreException ase) {
			// For experimentation - do nothing here at this time.
		// If either source or destination is null.
		} catch (NullPointerException npe) {
			// For experimentation - do nothing here at this time.
		}
	}

	public synchronized String getInitialID() {
		return new String(this.initialValue);
	}

	public synchronized String getCurrentID() {
		return new String(this.currentValue);
	}
	
	public synchronized String getNextID() {
	        
		// Get next values for each character, from right to left
		// (least significant to most significant).
		//
		// When reaching the maximum value for any character position,
		// 'roll over' to the minimum value for that position.
		for (int i = (this.currentValue.length - 1); i >= 0; i--) {
		
			switch (this.currentValue[i]) {
			
				case LOWERCASE_Z_CHAR:  // z
					if (i == 0) {
						throw new IllegalStateException(
							"The maximum number of IDs has been reached");
					}
					this.currentValue[i] = 'a';
					break;

			 case UPPERCASE_Z_CHAR:  // Z
					if (i == 0) {
						throw new IllegalStateException(
						"The maximum number of IDs has been reached");
					}
					this.currentValue[i] = 'A';
					break;

				default:
					this.currentValue[i]++;
					i = -1;
					break;
					
			} // end switch

		} // end 'for' loop
   
		return new String(currentValue);

  }
	
}
