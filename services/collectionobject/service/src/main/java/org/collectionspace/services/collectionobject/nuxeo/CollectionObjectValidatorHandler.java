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

import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class CollectionObjectValidatorHandler implements ValidatorHandler {

    final Logger logger = LoggerFactory.getLogger(CollectionObjectValidatorHandler.class);
    final boolean isValidationOn = false;

    @Override
    public void validate(Action action, ServiceContext ctx)
            throws InvalidDocumentException {
            	
				// This is a "fix" for CSPACE-1147 at issues.collectionspace.org
				if (isValidationOn == false) return;
				         	
        if(logger.isDebugEnabled()) {
            logger.debug("validate() action=" + action.name());
        }
        try {
            MultipartServiceContext mctx = (MultipartServiceContext) ctx;
            CollectionobjectsCommon co = (CollectionobjectsCommon) mctx.getInputPart(mctx.getCommonPartLabel(),
                    CollectionobjectsCommon.class);
            StringBuilder msgBldr = new StringBuilder("validate()");
            boolean invalid = false;
            if (co.getObjectNumber() == null || co.getObjectNumber().isEmpty()) {
                invalid = true;
                msgBldr.append("\nobjectNumber : missing");
            }
            if(action.equals(Action.CREATE)) {
                //create specific validation here
            } else if(action.equals(Action.UPDATE)) {
                //update specific validation here
            }

            if (invalid) {
                String msg = msgBldr.toString();
                logger.error(msg);
                throw new InvalidDocumentException(msg);
            }
        } catch (InvalidDocumentException ide) {
            throw ide;
        } catch (Exception e) {
            throw new InvalidDocumentException(e);
        }
    }
}
