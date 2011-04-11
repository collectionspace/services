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

/**
 * IDGeneratorInstance.
 *
 * $LastChangedRevision: 850 $
 * $LastChangedDate: 2009-10-12 15:17:09 -0700 (Mon, 12 Oct 2009) $
 */
public class IDGeneratorInstance {

    private String displayName;
    private String description;
    private String generatorState;
    private String lastGeneratedID;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGeneratorState() {
        return generatorState;
    }

    public void setGeneratorState(String generatorState) {
        this.generatorState = generatorState;
    }

    public String getLastGeneratedID() {
        return lastGeneratedID;
    }

    public void setLastGeneratedID(String lastGeneratedID) {
        this.lastGeneratedID = lastGeneratedID;
    }

}
