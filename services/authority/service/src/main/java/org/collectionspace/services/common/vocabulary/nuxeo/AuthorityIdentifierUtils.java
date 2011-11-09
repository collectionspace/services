/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.common.vocabulary.nuxeo;

import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.api.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * AuthorityIdentifierUtils
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class AuthorityIdentifierUtils {

    private final Logger logger = LoggerFactory.getLogger(AuthorityIdentifierUtils.class);

    // CSPACE-2215
    // FIXME: Consider replacing this with a different algorithm, perhaps one
    // that combines stems of each word token in the displayname.
    // FIXME: Verify uniqueness before returning the generated short identifier.
    // FIXME: Consider using a hash of the display name, rather than a timestamp,
    // when it is necessary to add a suffix for uniqueness.
    protected static String generateShortIdentifierFromDisplayName(String displayName, String shortDisplayName) {
        String generatedShortIdentifier = "";
        if (Tools.notEmpty(displayName)) {
            generatedShortIdentifier = displayName + '-' + Tools.now().toString();
        } else if (Tools.notEmpty(shortDisplayName)) {
            generatedShortIdentifier = shortDisplayName + '-' + Tools.now().toString();
        }
        // Ensure that the short identifier consists only of word chars.
        if (Tools.notEmpty(generatedShortIdentifier)) {
            generatedShortIdentifier = generatedShortIdentifier.replaceAll("[^\\w]", "");
        }
        // Fallback if we can't generate a short identifier from the displayname(s).
        if (generatedShortIdentifier.isEmpty()) {
            generatedShortIdentifier = java.util.UUID.randomUUID().toString();
        }
        return generatedShortIdentifier;
    }


}
