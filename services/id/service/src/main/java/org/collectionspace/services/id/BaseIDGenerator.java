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

// @TODO Add Javadoc comments

// @TODO Catch Exceptions thrown by IDGeneratorPart, then
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

    protected String csid = "";
    protected String uri = "";
    protected String description = "";
    protected Vector<IDGeneratorPart> parts = new Vector<IDGeneratorPart>();

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
     *
     * @return  A CollectionSpace ID (CSID) identifying this ID generator.
     */
    public String getCsid() {
        return this.csid;
    }

    /**
     * Sets a URI as a second type of identifier for this ID generator,
     * in addition to its CollectionSpace ID (CSID).
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
     * Returns a String representation of the URI, if any,
     * that is used as a second type of identifier for this
     * ID generator, in addition to its CollectionSpace ID (CSID).
     *
     * @return  A String representation of the URI identifying this
     *          ID generator.
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
     *
     * @return  description  A human-readable description of this ID generator.
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
    
        if (id == null) {
            return false;
        }
 
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
