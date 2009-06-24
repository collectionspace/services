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

// @TODO: When auto expanding, we'll need to set a maximum length to which the
// generated IDs can grow, likely as an additional parameter to be
// passed to a constructor, with a default value hard-coded in the class.

// @TODO: Consider handling escaped characters or sequences which represent Unicode
// code points, both in the start and end characters of the sequence, and in the initial value.
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
//
// Note also that, if the goal is to cycle through a sequence of alphabetic identifiers,
// such as the sequence of characters used in a particular human language, it may or may not
// be the case that any contiguous Unicode code point sequence reflects such a character sequence.

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
  private static final String DEFAULT_INITIAL_VALUE = "a";
	  
  private char startChar = NULL_CHAR;
  private char endChar = NULL_CHAR;
  
	private Vector<Character> initialValue = new Vector<Character>();
	private Vector<Character> currentValue = new Vector<Character>();

	// Constructor using defaults for character sequence and initial value.
	//
  // If no start and end characters are provided for the alphabetic character
  // sequence, default to an 'a-z' sequence, representing the lowercase alphabetic
  // characters in the USASCII character set (within Java's internal
  // Unicode UTF-16 representation).
  //
  // Additionally defaults to an initial value of "a".
	public AlphabeticIDGenerator() throws IllegalArgumentException {
	  
	  this(DEFAULT_START_CHAR, DEFAULT_END_CHAR, DEFAULT_INITIAL_VALUE);
	  
	}

	// Constructor using defaults for character sequence.
	//
  // If no start and end characters are provided for the alphabetic character
  // sequence, default to an 'a-z' sequence, representing the lowercase alphabetic
  // characters in the USASCII character set (within Java's internal
  // Unicode UTF-16 representation).
	public AlphabeticIDGenerator(String initial) throws IllegalArgumentException {
	  
	  this(DEFAULT_START_CHAR, DEFAULT_END_CHAR, initial);
	  
	}
	
	// Constructor.
	public AlphabeticIDGenerator(String sequenceStart, String sequenceEnd, String initial)
	  throws IllegalArgumentException {
	  
	  // Validate and store the start character in the character sequence.
	  
	  if (sequenceStart == null || sequenceStart.equals("")) {
			throw new IllegalArgumentException(
			  "Start character in the character sequence must not be null or empty");
		}
	  
	  if (sequenceStart.length() == 1) {
      this.startChar = sequenceStart.charAt(0);
    } else if (false) {
      // Handle representations of Unicode code points here
    } else {
			throw new IllegalArgumentException(
			  "Start character must be one character in length");
			  // "Start character must be one character in length or a Unicode value such as '\u0000'");
		}

	  // Validate and store the end character in the character sequence.

	  if (sequenceEnd == null || sequenceEnd.equals("")) {
			throw new IllegalArgumentException(
			  "End character in the character sequence must not be null or empty");
		}
	  
	  if (sequenceEnd.length() == 1) {
      this.endChar = sequenceEnd.charAt(0);
    } else if (false) {
      // Handle representations of Unicode code points here
    } else {
			throw new IllegalArgumentException(
			  "End character must be one character in length");
			  // "End character must be one character in length or a Unicode value such as '\u0000'");
		}
    
	  if (this.endChar <= this.startChar) {
			throw new IllegalArgumentException(
			  "End (last) character in the character sequence must be greater than the start character");
		}
		
	  // Validate and store the initial value of this identifier.

		if (initial == null || initial.equals("")) {
			throw new IllegalArgumentException("Initial value must not be null or empty");
		}
		
		// @TODO: Add a check for maximum length of the initial value here.
	
	  // Store the chars in the initial value as Characters in a Vector,
	  // validating each character to identify whether it falls within
	  // the provided sequence.
	  //
	  // (Since we're performing casts from char to Character, we can't just
	  // use Arrays.asList() to copy the initial array to a Vector.)
		char[] chars = initial.toCharArray();
		char ch;
		for (int i = 0; i < chars.length; i++) {

      // If the character falls within the provided sequence, copy it to the Vector.
      ch = chars[i];
			if (ch >= this.startChar && ch <= this.endChar) {
			  this.initialValue.add(new Character(ch));
      // Otherwise, we've detected a character not in the sequence.
			} else {
        throw new IllegalArgumentException("character " + "\'" + ch + "\'" + " is not valid");
      }
		  
		}

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

  // Sets the current value.
	public synchronized void setCurrentID(String value) throws IllegalArgumentException {
	
	  // @TODO Much of this code is copied from the main constructor,
	  // and may be ripe for refactoring.
	  
		if (value == null || value.equals("")) {
			throw new IllegalArgumentException("Initial value must not be null or empty");
		}
		
		// @TODO: Add a check for maximum length of the value here.
	
	  // Store the chars in the value as Characters in a Vector,
	  // validating each character to identify whether it falls within
	  // the provided sequence.
	  //
	  // (Since we're performing casts from char to Character, we can't just
	  // use Arrays.asList() to copy the initial array to a Vector.)
		char[] chars = value.toCharArray();
		char ch;
		Vector v = new Vector<Character>();
		for (int i = 0; i < chars.length; i++) {

      // If the character falls within the range bounded by the start and end
      // characters, copy it to the Vector.
      ch = chars[i];
			if (ch >= this.startChar && ch <= this.endChar) {
			  v.add(new Character(ch));
      // Otherwise, we've detected a character not in the sequence.
			} else {
        throw new IllegalArgumentException("character " + "\'" + ch + "\'" + " is not valid");
      }
		  
		}

		// Set the current value.
		this.currentValue = new Vector<Character>(v);
	}
	
	// Returns the next alphabetic ID in the sequence.
	//
  // Currently, the number of characters auto-expands as the
  // value of the most significant character rolls over.
  // E.g. a call to getNextID(), where the current ID is "z",
  // auto-expands to "aa", and "ZZ" auto-expands to "AAA".
  //
  // See the TODOs at the top of this class for additional
  // functionality that needs to be implemented.
  public synchronized String getNextID() {

		// Get next values for each character, from right to left
		// (least significant to most significant).
		boolean expandIdentifier = false;
		int size = this.currentValue.size();
		char ch;
		for (int i = (size - 1); i >= 0; i--) {

      ch = this.currentValue.get(i).charValue();

      // When we reach the maximum value for any character,
      // 'roll over' to the minimum value in our character range.
      if (ch == this.endChar) {
		    this.currentValue.set(i, Character.valueOf(this.startChar));
		    // If this rollover occurs in the most significant value,
		    // set a flag to later expand the size of the identifier.
		    //
		    // @TODO: Set another flag to enable or disable this behavior,
		    // as well as a mechanism for setting the maximum expansion permitted.
		    if (i == 0) {
		      expandIdentifier = true;
		    }
      // When we reach the most significant character whose value
      // doesn't roll over, increment that character and exit the loop.
		  } else {
        ch++;
        this.currentValue.set(i, Character.valueOf(ch));
        i = -1;
        break;		  
		  }

		}

    // If we are expanding the size of the identifier, insert a new
    // value at the most significant (leftmost) character position,
    // sliding other values to the right.
    if (expandIdentifier) {
      this.currentValue.add(0, Character.valueOf(this.startChar));
    }
		
		return getIDString(this.currentValue);
		
  }

  // Returns a String representation of the ID, by appending
  // the String values of each character in the Vector.
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
	  // @TODO: May need to constrain the number of alphabetic characters based
	  // on a maximum value, TBA.  Currently, this regex simply matches sequences
	  // of one or more characters.
		String regex = 
		  "(" + "[" + 
		  String.valueOf(this.startChar) + "-" + String.valueOf(this.endChar) +
		  "]+" + ")";
		return regex;
	}	
}
