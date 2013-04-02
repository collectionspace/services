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
package org.collectionspace.services.person.nuxeo;

import java.util.Arrays;
import java.util.List;

import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityItemDocumentModelHandler;
import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.person.PersonsCommon;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * PersonDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
/**
 * @author pschmitz
 *
 */
public class PersonDocumentModelHandler
	extends AuthorityItemDocumentModelHandler<PersonsCommon> {

    /** The logger. */
    //private final Logger logger = LoggerFactory.getLogger(PersonDocumentModelHandler.class);
    /**
     * Common part schema label
     */
    private static final String COMMON_PART_LABEL = "persons_common";
    
    public PersonDocumentModelHandler() {
    	super(COMMON_PART_LABEL);
    }

    @Override
    public String getAuthorityServicePath(){
        return PersonAuthorityClient.SERVICE_PATH_COMPONENT;    // CSPACE-3932
    }
	
    /**
     * Handle display names.
     *
     * @param docModel the doc model
     * @throws Exception the exception
     */
    /*
    @Override
    protected void handleComputedDisplayNames(DocumentModel docModel) throws Exception {
    	String commonPartLabel = getServiceContext().getCommonPartLabel("persons");
    	Boolean displayNameComputed = (Boolean) docModel.getProperty(commonPartLabel,
    			PersonJAXBSchema.DISPLAY_NAME_COMPUTED);
    	Boolean shortDisplayNameComputed = (Boolean) docModel.getProperty(commonPartLabel,
    			PersonJAXBSchema.SHORT_DISPLAY_NAME_COMPUTED);
    	if(displayNameComputed==null)
    		displayNameComputed = true;
    	if(shortDisplayNameComputed==null)
    		shortDisplayNameComputed = true;
    	if (displayNameComputed || shortDisplayNameComputed) {
    		String forename = 
				(String)docModel.getProperty(commonPartLabel, PersonJAXBSchema.FORE_NAME);
    		String lastname = 
				(String)docModel.getProperty(commonPartLabel, PersonJAXBSchema.SUR_NAME);
    		if(shortDisplayNameComputed) {
	    		String displayName = prepareDefaultDisplayName(forename, null, lastname,
	    				null, null);
	    		docModel.setProperty(commonPartLabel, PersonJAXBSchema.SHORT_DISPLAY_NAME,
	    				displayName);
    		}
    		if(displayNameComputed) {
	    		String midname = 
					(String)docModel.getProperty(commonPartLabel, PersonJAXBSchema.MIDDLE_NAME);    			
	    		String birthdate = 
					(String)docModel.getProperty(commonPartLabel, PersonJAXBSchema.BIRTH_DATE);
	    		String deathdate = 
					(String)docModel.getProperty(commonPartLabel, PersonJAXBSchema.DEATH_DATE);
	    		String displayName = prepareDefaultDisplayName(forename, midname, lastname,
	    				birthdate, deathdate);
	    		docModel.setProperty(commonPartLabel, PersonJAXBSchema.DISPLAY_NAME,
	    				displayName);
    		}
    	}
    }
    * 
    */
	
	
    /**
     * Produces a default displayName from the basic name and dates fields.
     * see PersonAuthorityClientUtils.prepareDefaultDisplayName(String,String,String,String,String) which
     * duplicates this logic, until we define a service-general utils package
     * that is neither client nor service specific.
     * @param foreName	
     * @param middleName
     * @param surName
     * @param birthDate
     * @param deathDate
     * @return
     * @throws Exception
     */
    /*
    private static String prepareDefaultDisplayName(
    		String foreName, String middleName, String surName,
    		String birthDate, String deathDate ) throws Exception {
		final String SEP = " ";
		final String DATE_SEP = "-";

		StringBuilder newStr = new StringBuilder();
		List<String> nameStrings = 
			Arrays.asList(foreName, middleName, surName);
		boolean firstAdded = false;
    	for (String partStr : nameStrings ){
			if (partStr != null) {
				if (firstAdded == true) {
					newStr.append(SEP);
				}
				newStr.append(partStr);
				firstAdded = true;
			}
    	}
    	// Now we add the dates. In theory could have dates with no name, but that is their problem.
    	boolean foundBirth = false;
		if (birthDate != null) {
			if (firstAdded) {
				newStr.append(SEP);
			}
			newStr.append(birthDate);
	    	newStr.append(DATE_SEP);		// Put this in whether there is a death date or not
			foundBirth = true;
		}
		if (deathDate != null) {
			if (!foundBirth) {
				if (firstAdded == true) {
					newStr.append(SEP);
				}
		    	newStr.append(DATE_SEP);
			}
			newStr.append(deathDate);
		}
		
		return newStr.toString();
    }
    * 
    */
    
    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    @Override
    public String getQProperty(String prop) {
        return PersonConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

