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

package org.collectionspace.services.id;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.collectionspace.services.common.document.BadRequestException;

/**
 * UUIDGeneratorPart
 *
 * Generates universally unique identifiers (UUIDs) in the Version 4 format
 * (primarily consisting of random or pseudo-random numbers), described at
 * http://en.wikipedia.org/wiki/Universally_Unique_Identifier#Version_4_.28random.29
 *
 * $LastChangedRevision: 625 $
 * $LastChangedDate$
 */
public class UUIDGeneratorPart implements IDGeneratorPart {
    
    String currentValue = "";
    
    final static String REGEX_PATTERN =
        "(" +
        "[a-z0-9\\-]{8}" +
        "\\-" +
        "[a-z0-9\\-]{4}" +
        "\\-" +
        "4" +
        "[a-z0-9\\-]{3}" +
        "\\-" +
        "[89ab]" +
        "[a-z0-9\\-]{3}" +
        "\\-" +
        "[a-z0-9\\-]{12}" +
        ")";
        
    public final static int UUID_LENGTH = 36;
    
    /**
     * Constructor (no-argument).
     */
    public UUIDGeneratorPart() {
    }

    @Override
    public void setCurrentID(String idValue) throws BadRequestException {
		if (idValue == null || idValue.equals("")) {
			throw new BadRequestException(
			    "Supplied UUID must not be null or empty");
		}
		if (! isValidID(idValue)) {
			throw new BadRequestException(
			    "Supplied UUID '" + idValue + "' is not valid.");
        }
        this.currentValue = idValue;
    }

    @Override
    public String getCurrentID() {
        if (this.currentValue == null || this.currentValue.equals("")) {
            return newID(); // Will also set the current ID to the new ID.
        } else {
            return this.currentValue;
        }
    }
    
    @Override
    public String newID() {
        String newID = UUID.randomUUID().toString();
        try {
            setCurrentID(newID);
        } catch (BadRequestException ex) {
            // Do nothing.  There will be no adverse consequences if we
            // can't set the current ID here.
        }
        return newID;
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
    
}
