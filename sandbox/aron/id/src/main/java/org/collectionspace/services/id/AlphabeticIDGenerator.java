/*	
 * AlphabeticIDGenerator
 *
 * <p>An identifier generator that generates an incrementing alphabetic ID
 * from any sequence of characters, as a String object.</p>
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
// Currently, identifiers simply 'wrap' (roll over) within that fixed
// series, which is not always the desired outcome.
//
// We may also need to model cases where the number of characters
// auto-expands as the value of the most significant character
// rolls over, up to a specified maximum number of characters:
// e.g. a call to getNextID(), where the current ID is "z",
// auto-expands to "aa", and a current ID of "ZZ" auto-expands to "AAA".
//
// When doing so, we'll also need to set a maximum length to which the
// generated IDs can grow, likely as an additional parameter to be
// passed to a constructor, with a default value hard-coded in the class.

// @TODO: Handle escaped characters or sequences which represent Unicode code points,
// both in the start and end characters of the sequence, and in the initial value.
// (Example: '\u0072' for the USASCII 'r' character; see
// http://www.fileformat.info/info/unicode/char/0072/index.htm)
//
// Ideally, we should read these in free-text patterns, alongside unescaped characters,
// but in practice we may wish to require some structured form for arguments
// containing such characters.
//
// Some initial research on this:
// http://www.velocityreviews.com/forums/t367758-unescaping-unicode-code-points-in-a-java-string.html
// We might also look into the (protected) source code for java.util.Properties.load()
// which reads escaped Unicode values.

// NOTE: This class currently hard-codes the assumption that the values in
// alphabetic identifiers are ordered in significance from left-to-right;
// that is, the most significant value appears in the left-most position.
 
package org.collectionspace.services.id;

import java.util.Collections;
import java.util.Vector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlphabeticIDGenerator implements IDGenerator {
   
  private static final char NULL_CHAR = '\u0000';

  private static final String DEFAULT_START_CHAR = "a";
  private static final String DEFAULT_END_CHAR = "z";
	  
  private char startChar = NULL_CHAR;
  private char endChar = NULL_CHAR;
  
	private Vector<Character> initialValue = new Vector<Character>();
	private Vector<Character> currentValue = new Vector<Character>();

  // Defaults to an 'a-z' series, representing lowercase alphabetic characters
  // in the USASCII character set within Java's internal representation of
  // characters (Unicode's UTF-16 encoding), if no start and end characters
  // are provided for the alphabetic character sequence.
	public AlphabeticIDGenerator(String initial) throws IllegalArgumentException {
	  
	  this(DEFAULT_START_CHAR, DEFAULT_END_CHAR, initial);
	  
	}
	
	public AlphabeticIDGenerator(String seriesStart, String seriesEnd, String initial)
	  throws IllegalArgumentException {
	  
	  // Validate and store the start character in the alphabetic series.
	  
	  if (seriesStart == null || seriesStart.equals("")) {
			throw new IllegalArgumentException(
			  "Start character in the alphabetic series must not be null or empty");
		}
	  
    // @TODO The next two statements will need to be revised to handle escaped
    // representations of characters outside the USASCII character set.
	  if (seriesStart.length() > 1) {
			throw new IllegalArgumentException(
			  "Start character in the alphabetic series must be exactly one character in length");
		}

    this.startChar = seriesStart.charAt(0);

	  // Validate and store the end character in the alphabetic series.

	  if (seriesEnd == null || seriesEnd.equals("")) {
			throw new IllegalArgumentException(
			  "End character in the alphabetic series must not be null or empty");
		}
	  
    // @TODO The next two statements will need to be revised to handle escaped
    // representations of characters outside the USASCII character set.
	  if (seriesEnd.length() > 1) {
			throw new IllegalArgumentException(
			  "End character in the alphabetic series must be exactly one character in length");
		}

    this.endChar = seriesEnd.charAt(0);
    
	  if (this.endChar <= this.startChar) {
			throw new IllegalArgumentException(
			  "End (last) character in an alphabetic series must be greater than the start character");
		}
		
	  // Validate and store the initial value of this identifier.

		if (initial == null || initial.equals("")) {
			throw new IllegalArgumentException("Initial value must not be null or empty");
		}
		
		// @TODO: Add a check for maximum length of the initial value here.
	
	  // Store the chars in the initial value as Characters in a Vector.
	  // (Since we're performing casts from char to Character, we can't just
	  // use Arrays.asList() to copy the initial array to a Vector.)
		char[] chars = initial.toCharArray();
		for (int i=0; i < chars.length; i++) {
		  this.initialValue.add(new Character(chars[i]));
		}
		
		// Validate that each of the characters in the initial value
		// falls within the provided series.
		for ( Character ch : this.initialValue ) {
		
			if (ch.charValue() >= this.startChar && ch.charValue() <= this.endChar) {
			  continue;
      // Otherwise, we've detected a character not in the series.
			} else {
        throw new IllegalArgumentException("character " + "\'" + ch + "\'" + " is not valid");
      }
				
		} // end 'for' loop
		
		// Initialize the current value from the initial value.
		this.currentValue = new Vector<Character>(this.initialValue);

	}

  // Reset the current value to the initial value.
	public synchronized void reset() {
	  Collections.copy(this.currentValue, this.initialValue);
	}

  // Returns the initial value.
	public synchronized String getInitialID() {
		return getIDString(this.initialValue);
	}

  // Returns the current value.
	public synchronized String getCurrentID() {
		return getIDString(this.currentValue);
	}
	
	// Returns the next alphabetic ID in the series.
	public synchronized String getNextID() {
		        
		// Get next values for each character, from right to left
		// (least significant to most significant).
		boolean expandIdentifier = false;
		int size = this.currentValue.size();
		char c;
		for (int i = (size - 1); i >= 0; i--) {

      c = this.currentValue.get(i).charValue();

      // When reaching the maximum value for any character position,
      // 'roll over' to the minimum value for that position.
      if (c == this.endChar) {
		    this.currentValue.set(i, Character.valueOf(this.startChar));
		    // If this roll over occurs in the most significant value,
		    // set a flag to later expand the size of the identifier.
		    //
		    // @TODO: Set another flag to enable or disable this behavior,
		    // as well as a mechanism for setting the maximum expansion permitted.
		    if (i == 0) {
		      expandIdentifier = true;
		    }
		  } else {
        c++;
        this.currentValue.set(i, Character.valueOf(c));
        i = -1;
        break;		  
		  }

		}

    // If we are expanding the size of the identifier, insert a new
    // value at the most significant character position, sliding other
    // values to the right.
    if (expandIdentifier) {
      this.currentValue.add(0, Character.valueOf(this.startChar));
    }
		
		return getIDString(this.currentValue);
		
  }

  // Returns a String representation of the contents of a Vector,
  // in the form of an identifier (e.g. each Character's String value
  // is appended to the next).
  public synchronized String getIDString(Vector<Character> v) {
		StringBuffer sb = new StringBuffer();
	  for ( Character ch : v ) {
      sb.append(ch.toString());
		}
		return sb.toString();
	}

	public synchronized boolean isValidID(String value) throws IllegalArgumentException {

		if ( value == null || value == "") {
			throw new IllegalArgumentException("ID to validate must not be null or empty");
		}

		Pattern pattern = Pattern.compile(getRegex());
		Matcher matcher = pattern.matcher(value);
		if (matcher.matches()) {
			return true;
		} else {
			return false;
		}
		
	}

	public synchronized String getRegex() {
		// @TODO: This method is stubbed out; it needs to be implemented.
		String regex = "(" + "\\*" + ")";
		return regex;
	}	
}
