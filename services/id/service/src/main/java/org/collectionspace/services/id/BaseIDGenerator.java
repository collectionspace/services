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

// @TODO: Catch Exceptions thrown by IDGeneratorPart, then
// reflect this in the corresponding BaseIDGeneratorTest class.

package org.collectionspace.services.id;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BaseIDGenerator
 *
 * Models an identifier (ID) generator, consisting of 
 * multiple IDGeneratorParts.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class BaseIDGenerator implements IDGenerator {

    private String csid = "";
    private String uri = "";
    private String description = "";
    private Vector<IDGeneratorPart> parts = new Vector<IDGeneratorPart>();

    final static int MAX_ID_LENGTH = 50;
    
    /**
     * Constructor.
     *
     * @param csid  A CollectionSpace ID (CSID) identifying this ID generator.
     *
     */
    public BaseIDGenerator(String csid) {
      if (csid != null && ! csid.equals("")) {
        this.csid = csid;
      }
    }
    
    /**
     * Constructor.
     *
     * @param csid  A CollectionSpace ID (CSID) identifying this ID generator.
     *
     * @param parts A collection of ID generator parts.
     *
     */
    public BaseIDGenerator(String csid, Vector<IDGeneratorPart> parts) {
        if (csid != null && ! csid.equals("")) {
            this.csid = csid;
        }
        if (parts != null) {
            this.parts = parts;
        }
    }

    /**
     * Returns the CollectionSpace ID (CSID) identifying this ID generator.
     */
    public String getCsid() {
        return this.csid;
    }

    /**
     * Sets a URI as a second type of identifier for this ID generator,
     * in addition to the CollectionSpace ID (CSID).
     *
     * @param uriStr A String representation of a URI.
     */
    public void setURI(String uriStr) {
        if (uriStr == null || uriStr.equals("")) {
            return;
        }
        // Validate that this is a legal URI.
        try {
            URI uri = new URI(uriStr);
        } catch (URISyntaxException e) {
            // Fail silently without setting the URI.
            return;
        }
        this.uri = uriStr;
    }

    /**
     * Returns the URI, if any, that is used as a second type of
     * identifier for this ID generator.
     */
    public String getURI() {
        return this.uri;
    }

    /**
     * Sets an optional, human-readable description of this ID generator.
     *
     * @param description A human-readable description of this ID generator.
     */
    public void setDescription(String description) {
        if (description != null) {
            this.description = description;
        }
    }

    /**
     * Returns the human-readable description of this ID generator, if any.
     */
    public String getDescription() {
        return this.description;
    }
  
    /**
     * Adds a single ID generator part.
     *
     * @param part  An ID generator part.
     */
    public void add(IDGeneratorPart part) {
        if (part != null) {
            this.parts.add(part);
        }
    }

    /**
     * Removes all ID generator parts.
     */
    public void clear() {
        this.parts.clear();
    }

    @Override
    public String getCurrentID() {
        StringBuffer sb = new StringBuffer(MAX_ID_LENGTH);
        for (IDGeneratorPart part : this.parts) {
            sb.append(part.getCurrentID());
        }
        return sb.toString();
    }


/*
    @Override
    public String newID() {
        StringBuffer sb = new StringBuffer(MAX_ID_LENGTH);
        int lastPart = parts.size() - 1;
        int i = 0;
        for (IDGeneratorPart part : this.parts) {
            if (i == lastPart){
                sb.append(part.newID());
            } else {
                sb.append(part.getCurrentID());
            }
            i++;
        }
        return sb.toString();
    }
*/

    // Returns the current value of this ID, given a
    // supplied ID that partly matches the pattern.
    //
    // If the supplied ID fully matches the pattern,
    // will return the supplied ID.
    //
    // However, if the supplied ID is a partial ID, which
    // partly "stem-matches" the pattern but does not
    // ully match the pattern, will return the partial ID with
    // its next ID component appended.  The next ID component
    // will be set to its initial value.
    //
    // Examples:
    // * 2009.5." becomes "2009.5.1", in a case where the
    //   next ID component is an incrementing numeric IDGeneratorPart.
    // * "E55-" becomes "E55-a", where the next ID component
    //   is an incrementing alphabetic IDGeneratorPart.
    public String getCurrentID(String value)
        throws IllegalArgumentException {

      if (value == null) return value;
      
      // Try ever-larger stem matches against the supplied value,
      // by incrementally appending each part's regex, until no
      // (more) matches are found.
      //
      // In so doing, build a subset of this BaseIDGenerator's regex
      // that fully matches the supplied value.
      Pattern pattern = null;
      Matcher matcher = null;
      int matchedParts = 0;
      StringBuffer regexToTry = new StringBuffer();
      StringBuffer regex = new StringBuffer();
      for (IDGeneratorPart partToTryMatching : this.parts) {
          regexToTry.append(partToTryMatching.getRegex());
          pattern = Pattern.compile(regexToTry.toString());
            matcher = pattern.matcher(value);
            // If a stem match was found on the current regex,
            // store a count of matched IDGeneratorParts and the regex pattern
            // that has matched to this point.
            if (matcher.lookingAt()) {
                matchedParts++;
              regex.append(partToTryMatching.getRegex());
            // Otherwise, exit the loop.
            } else {
                break;
            }
        }

        // If the supplied ID doesn't partly match the pattern,
        // throw an Exception.
        if (matchedParts == 0) {
            throw new IllegalArgumentException("Supplied ID does not match this ID pattern.");
        }

        pattern = Pattern.compile(regex.toString());
        matcher = pattern.matcher(value);
        
        // If the supplied ID doesn't match the pattern built above,
        // throw an Exception.  (This error condition should likely
        // never be reached, but it's here as a guard.)
        if (! matcher.matches()) {
            throw new IllegalArgumentException("Supplied ID does not match this ID pattern.");
        }
        
        // Otherwise, if the supplied ID matches the pattern,
        // split the ID into its components and store those
        // values in each of the pattern's IDGeneratorParts.
        IDGeneratorPart currentPart;
        for (int i = 1; i <= matchedParts; i++) {
            currentPart = this.parts.get(i - 1);
            currentPart.setCurrentID(matcher.group(i));
        }

        // Obtain the initial value of the next IDGeneratorPart, and
        // set the current value of that part to its initial value.
        //
        // If the supplied ID fully matches the pattern, there will
        // be no 'next' IDGeneratorPart, and we must catch that Exception below. 
        int nextPartNum = matchedParts;
        try {
            // String initial = this.parts.get(nextPartNum).getInitialID();
            // this.parts.get(nextPartNum).setCurrentID(initial);
            String currentID = this.parts.get(nextPartNum).getCurrentID();
            // Increment the number of matched parts to reflect the
            // addition of this next IDGeneratorPart.
            matchedParts++;
        } catch (ArrayIndexOutOfBoundsException e ) {
            // Do nothing here; we simply won't increment
            // the number of matched parts, used in the loop below.
        }
        
        // Call the getCurrentID() method on each of the
        // supplied IDGeneratorParts, as well as on the added IDGeneratorPart
        // whose initial value was just obtained, if any.
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i <= matchedParts; i++) {
            sb.append(this.parts.get(i - 1).getCurrentID());
        }
        
        return sb.toString();

    }

    // Returns the next value of this ID, and sets the current value to that ID.
    @Override
    public String newID() throws IllegalStateException {
    
        int lastPartNum = this.parts.size();
        StringBuffer sb = new StringBuffer(MAX_ID_LENGTH);
        int i = 0;
        for (IDGeneratorPart part : this.parts) {
        	i++;
        	if (i < lastPartNum) {
            	sb.append(part.getCurrentID());
           	} else {
           		sb.append(part.newID());
           	}
        }
        
        return sb.toString();
        
    }


    // Returns the new value of this ID, given a
    // supplied ID that entirely matches the pattern,
    // and sets the current value to that ID.
    public String newID(String value)
        throws IllegalStateException, IllegalArgumentException {

        if (value == null) { 
            throw new IllegalArgumentException("Supplied ID cannot be null.");
        }
        
        Pattern pattern = Pattern.compile(getRegex());
        Matcher matcher = pattern.matcher(value);
        
        // If the supplied ID doesn't entirely match the pattern,
        // throw an Exception.
        if (! matcher.matches()) {
            throw new IllegalArgumentException(
                "Supplied ID does not match this ID pattern.");
        }
        
        // Otherwise, if the supplied ID entirely matches the pattern,
        // split the ID into its components and store those values in
        // each of the pattern's IDGeneratorParts.
        IDGeneratorPart currentPart;
        for (int i = 1; i <= (matcher.groupCount() - 1); i++) {
            currentPart = this.parts.get(i - 1);
            currentPart.setCurrentID(matcher.group(i));
        }
        
        // @TODO This code is duplicated in newID(), above,
        // and thus we may want to refactor this.
        int lastPartNum = this.parts.size();
        StringBuffer sb = new StringBuffer(MAX_ID_LENGTH);
        int i = 0;
        for (IDGeneratorPart part : this.parts) {
        	i++;
        	if (i < lastPartNum) {
            	sb.append(part.getCurrentID());
           	} else {
           		sb.append(part.newID());
           	}
        }
        
        return sb.toString();
        
    }
    
 
    @Override
    public boolean isValidID(String id) {
        return isValidID(id, getRegex());
    }

   /**
     * Validates a supplied ID against the format of the IDs
     * generated by this ID generator.
     *
     * @param    id    An ID.
     *
     * @param    regex  A regular expression for validating IDs against
     *                  a format.
     *
     * @return   true if the supplied ID matches the format of the
     *           IDs generated by this ID generator;
     *           false if it does not match that format.
      */
    public boolean isValidID(String id, String regex) {
    
        if (id == null) return false;
 
        // @TODO May potentially throw at least one pattern-related exception.
        // We'll need to catch and handle this here, as well as in all
        // derived classes and test cases that invoke validation.

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(id);
        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
        
    }

    @Override
    public String getRegex() {
        StringBuffer sb = new StringBuffer();
        for (IDGeneratorPart part : this.parts) {
            sb.append(part.getRegex());
        }
        return sb.toString();
    }
 
}
