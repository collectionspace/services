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

import java.util.Iterator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityItemDocumentModelHandler;
import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.person.PersonsCommon;
import org.collectionspace.services.person.PersonsCommonList;
import org.collectionspace.services.person.PersonsCommonList.PersonListItem;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

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
	extends AuthorityItemDocumentModelHandler<PersonsCommon, PersonsCommonList> {

    /** The logger. */
    //private final Logger logger = LoggerFactory.getLogger(PersonDocumentModelHandler.class);
    /**
     * Common part schema label
     */
    private static final String COMMON_PART_LABEL = "persons_common";
    
    public PersonDocumentModelHandler() {
    	super(COMMON_PART_LABEL);
    }
	
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#handleCreate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	// first fill all the parts of the document
    	super.handleCreate(wrapDoc);    	
    	handleDisplayNames(wrapDoc.getWrappedObject());
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#handleUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	super.handleUpdate(wrapDoc);
    	handleDisplayNames(wrapDoc.getWrappedObject());
    }

    /**
     * Handle display names.
     *
     * @param docModel the doc model
     * @throws Exception the exception
     */
    private void handleDisplayNames(DocumentModel docModel) throws Exception {
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
    

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#extractPart(org.nuxeo.ecm.core.api.DocumentModel, java.lang.String, org.collectionspace.services.common.service.ObjectPartType)
     */
    @Override
    protected Map<String, Object> extractPart(DocumentModel docModel, String schema, ObjectPartType partMeta)
            throws Exception {
    	Map<String, Object> unQObjectProperties = super.extractPart(docModel, schema, partMeta);
    	
    	// Add the CSID to the common part
    	if (partMeta.getLabel().equalsIgnoreCase(COMMON_PART_LABEL)) {
	    	String csid = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
	    	unQObjectProperties.put("csid", csid);
    	}
    	
    	return unQObjectProperties;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
	public PersonsCommonList extractCommonPartList(
			DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
		PersonsCommonList coList = extractPagingInfo(new PersonsCommonList(), wrapDoc);
        AbstractCommonList commonList = (AbstractCommonList) coList;
        commonList.setFieldsReturned("displayName|refName|shortIdentifier|uri|csid");
		List<PersonsCommonList.PersonListItem> list = coList.getPersonListItem();
		Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
		String commonPartLabel = getServiceContext().getCommonPartLabel(
				"persons");
		while (iter.hasNext()) {
			DocumentModel docModel = iter.next();
			PersonListItem ilistItem = new PersonListItem();
			ilistItem.setDisplayName((String) docModel.getProperty(
					commonPartLabel, PersonJAXBSchema.DISPLAY_NAME));
			ilistItem.setShortIdentifier((String) docModel.getProperty(commonPartLabel,
					PersonJAXBSchema.SHORT_IDENTIFIER));
			ilistItem.setRefName((String) docModel.getProperty(commonPartLabel,
					PersonJAXBSchema.REF_NAME));
			String id = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
			ilistItem.setUri("/personauthorities/" + inAuthority + "/items/"
					+ id);
			ilistItem.setCsid(id);
			list.add(ilistItem);
		}

		return coList;
	}

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

