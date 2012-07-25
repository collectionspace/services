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
 *//**
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.collectionobject.nuxeo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
//import org.collectionspace.services.common.context.MultipartServiceContext;
//import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class CollectionObjectValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut> {

    final Logger logger = LoggerFactory.getLogger(CollectionObjectValidatorHandler.class);

    //
    // Error Strings
    //
    private static final String VALIDATION_ERROR = "The collection object record payload was invalid. See log file for more details.";
    private static final String OBJECTNUMBER_NULL_ERROR = "The collection object field \"objectNumber\" cannot be empty or missing.";

    @Override
    protected Class<?> getCommonPartClass() {
    	return CollectionobjectsCommon.class;
    }
    
	@Override
	protected void handleGet(){
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleGetAll() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleUpdate()
			throws InvalidDocumentException {
    	try {
	        // PAHMA-473: Disable non-empty objectNumber requirement, so that updates don't need to retrieve the current objectNumber.
            //CollectionobjectsCommon co = (CollectionobjectsCommon) getCommonPart();
            //validateCollectionobjectsCommon(co);                        
    	} catch (AssertionError e) {
    		if (logger.isErrorEnabled() == true) {
    			logger.error(e.getMessage(), e);
    		}
    		throw new InvalidDocumentException(VALIDATION_ERROR, e);
    	}
	}

	@Override
	protected void handleDelete() {
		// TODO Auto-generated method stub
		
	}
    
    @Override
    protected void handleCreate()
    		throws InvalidDocumentException {
    	try {
            CollectionobjectsCommon co = (CollectionobjectsCommon) getCommonPart();
            validateCollectionobjectsCommon(co);                        
    	} catch (AssertionError e) {
    		if (logger.isErrorEnabled() == true) {
    			logger.error(e.getMessage(), e);
    		}
    		throw new InvalidDocumentException(VALIDATION_ERROR, e);
    	}
    }
    
    //
    // Private Methods
    //    
    private void validateCollectionobjectsCommon(CollectionobjectsCommon co) throws AssertionError {
    	CS_ASSERT(co != null);
        String objectNumber = co.getObjectNumber();
        CS_ASSERT(objectNumber != null);
        CS_ASSERT(objectNumber.isEmpty() == false, OBJECTNUMBER_NULL_ERROR);

    }
}
