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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.person.PersonsCommon;
import org.collectionspace.services.person.PersonsCommonList;
import org.collectionspace.services.person.PersonsCommonList.PersonListItem;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        extends RemoteDocumentModelHandlerImpl<PersonsCommon, PersonsCommonList> {

    private final Logger logger = LoggerFactory.getLogger(PersonDocumentModelHandler.class);
    /**
     * Common part schema label
     */
    private static final String COMMON_PART_LABEL = "persons_common";
    
    /**
     * person is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private PersonsCommon person;
    /**
     * personList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private PersonsCommonList personList;
    
    /**
     * inAuthority is the parent OrgAuthority for this context
     */
    private String inAuthority;

    public String getInAuthority() {
		return inAuthority;
	}

	public void setInAuthority(String inAuthority) {
		this.inAuthority = inAuthority;
	}

	
    @Override
    public void handleCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	// first fill all the parts of the document
    	super.handleCreate(wrapDoc);    	
    	handleDisplayName(wrapDoc.getWrappedObject());
    }
    
    @Override
    public void handleUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	super.handleUpdate(wrapDoc);
    	handleDisplayName(wrapDoc.getWrappedObject());
    }

    private void handleDisplayName(DocumentModel docModel) throws Exception {
    	String commonPartLabel = getServiceContext().getCommonPartLabel("persons");
    	Boolean displayNameComputed = (Boolean) docModel.getProperty(commonPartLabel,
    			PersonJAXBSchema.DISPLAY_NAME_COMPUTED);
    	if (displayNameComputed) {
    		String displayName = prepareDefaultDisplayName(
			(String)docModel.getProperty(commonPartLabel, PersonJAXBSchema.FORE_NAME ),    			
			(String)docModel.getProperty(commonPartLabel, PersonJAXBSchema.MIDDLE_NAME ),    			
			(String)docModel.getProperty(commonPartLabel, PersonJAXBSchema.SUR_NAME ),    			
			(String)docModel.getProperty(commonPartLabel, PersonJAXBSchema.BIRTH_DATE ),    			
			(String)docModel.getProperty(commonPartLabel, PersonJAXBSchema.DEATH_DATE )  			
			);
			docModel.setProperty(commonPartLabel, PersonJAXBSchema.DISPLAY_NAME,
					displayName);
    	}
    }
	
    /**
     * Produces a default displayName from the basic name and dates fields.
     * @see PersonAuthorityClientUtils.prepareDefaultDisplayName() which
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
    	StringBuilder newStr = new StringBuilder();
		final String sep = " ";
		final String dateSep = "-";
		List<String> nameStrings = 
			Arrays.asList(foreName, middleName, surName);
		boolean firstAdded = false;
    	for(String partStr : nameStrings ){
			if(null != partStr ) {
				if(firstAdded) {
					newStr.append(sep);
				}
				newStr.append(partStr);
				firstAdded = true;
			}
    	}
    	// Now we add the dates. In theory could have dates with no name, but that is their problem.
    	boolean foundBirth = false;
		if(null != birthDate) {
			if(firstAdded) {
				newStr.append(sep);
			}
			newStr.append(birthDate);
	    	newStr.append(dateSep);		// Put this in whether there is a death date or not
			foundBirth = true;
		}
		if(null != deathDate) {
			if(!foundBirth) {
				if(firstAdded) {
					newStr.append(sep);
				}
		    	newStr.append(dateSep);
			}
			newStr.append(deathDate);
		}
		return newStr.toString();
    }
    
    /**
     * getCommonPart get associated person
     * @return
     */
    @Override
    public PersonsCommon getCommonPart() {
        return person;
    }

    /**
     * setCommonPart set associated person
     * @param person
     */
    @Override
    public void setCommonPart(PersonsCommon person) {
        this.person = person;
    }

    /**
     * getCommonPartList get associated person (for index/GET_ALL)
     * @return
     */
    @Override
    public PersonsCommonList getCommonPartList() {
        return personList;
    }

    @Override
    public void setCommonPartList(PersonsCommonList personList) {
        this.personList = personList;
    }

    @Override
    protected Map<String, Object> extractPart(DocumentModel docModel, String schema, ObjectPartType partMeta)
            throws Exception {
    	Map<String, Object> unQObjectProperties = super.extractPart(docModel, schema, partMeta);
    	
    	// Add the CSID to the common part
    	if (partMeta.getLabel().equalsIgnoreCase(COMMON_PART_LABEL)) {
	    	String csid = NuxeoUtils.extractId(docModel.getPathAsString());
	    	unQObjectProperties.put("csid", csid);
    	}
    	
    	return unQObjectProperties;
    }
    
    @Override
    public PersonsCommon extractCommonPart(DocumentWrapper wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillCommonPart(PersonsCommon personObject, DocumentWrapper wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public PersonsCommonList extractCommonPartList(DocumentWrapper wrapDoc) 
    	throws Exception {
        PersonsCommonList coList = new PersonsCommonList();
        try{
	        DocumentModelList docList = (DocumentModelList) wrapDoc.getWrappedObject();
	
	        List<PersonsCommonList.PersonListItem> list = 
	        	coList.getPersonListItem();
	
	        DocumentFilter filter = getDocumentFilter();
	        long pageNum, pageSize;
	        if(filter==null) {
	        	pageNum = 0;
	        	pageSize = 0;
	        } else {
	        	pageSize = filter.getPageSize();
	        	pageNum = filter.getOffset()/pageSize;
	        }
	        coList.setPageNum(pageNum);
	        coList.setPageSize(pageSize);
	    	coList.setTotalItems(docList.totalSize());
	        //FIXME: iterating over a long list of documents is not a long term
	        //strategy...need to change to more efficient iterating in future
	        Iterator<DocumentModel> iter = docList.iterator();
	        String commonPartLabel = getServiceContext().getCommonPartLabel("persons");
	        while(iter.hasNext()){
	            DocumentModel docModel = iter.next();
	            PersonListItem ilistItem = new PersonListItem();
				ilistItem.setDisplayName((String) 
	            		docModel.getProperty(commonPartLabel, PersonJAXBSchema.DISPLAY_NAME));
	            ilistItem.setRefName((String) 
            		docModel.getProperty(commonPartLabel, PersonJAXBSchema.REF_NAME));
				String id = NuxeoUtils.extractId(docModel.getPathAsString());
	            ilistItem.setUri("/personauthorities/"+inAuthority+"/items/" + id);
	            ilistItem.setCsid(id);
	            list.add(ilistItem);
	        }
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception in extractCommonPartList", e);
            }
            throw e;
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

