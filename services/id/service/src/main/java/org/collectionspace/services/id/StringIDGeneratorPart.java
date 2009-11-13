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

// @TODO: Need to set and enforce maximum String length.

package org.collectionspace.services.id;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.collectionspace.services.common.document.BadRequestException;

/**
 * StringIDGeneratorPart
 *
 * Generates identifiers (IDs) that consist of a static String value.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class StringIDGeneratorPart implements IDGeneratorPart, 
    StoredValueIDGeneratorPart {
    
    private String initialValue = null;
    private String currentValue = null;
    
    public StringIDGeneratorPart(String initialValue)
        throws BadRequestException {

        if (initialValue == null || initialValue.equals("")) {
            throw new BadRequestException(
                "Initial ID value must not be null or empty");
        }
        
        this.initialValue = initialValue;
        this.currentValue = initialValue;

    }

    @Override
    public String getInitialID() {
        return this.initialValue;
    }

    @Override
    public String getCurrentID() {
        return this.currentValue;
    }

    public void setCurrentID(String value) throws BadRequestException {
        if (value == null || value.equals("")) {
            throw new BadRequestException(
            "ID value must not be null or empty");
        }
        this.currentValue = value;
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

        String initial = this.initialValue;
        
        // Escape or otherwise modify various characters that have
        // significance in regular expressions.
        //
        // @TODO Test these thoroughly, add processing of more
        // special characters as needed.
        
        // Escape un-escaped period/full stop characters.
        Pattern pattern = Pattern.compile("([^\\\\]{0,1})\\.");
        Matcher matcher = pattern.matcher(initial);
        String escapedInitial = matcher.replaceAll("$1\\\\.");

        String regex = "(" + escapedInitial + ")";
        return regex;
    }
    
}
