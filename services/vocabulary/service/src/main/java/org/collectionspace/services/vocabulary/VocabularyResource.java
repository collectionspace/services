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
package org.collectionspace.services.vocabulary;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.VocabularyClient;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
//import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
//import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.vocabulary.nuxeo.VocabularyItemDocumentModelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;

@Path("/" + VocabularyClient.SERVICE_PATH_COMPONENT)
public class VocabularyResource extends 
	AuthorityResource<VocabulariesCommon, VocabularyItemDocumentModelHandler> {

    private final static String vocabularyServiceName = VocabularyClient.SERVICE_PATH_COMPONENT;

	private final static String VOCABULARIES_COMMON = "vocabularies_common";
    
    private final static String vocabularyItemServiceName = "vocabularyitems";
	private final static String VOCABULARYITEMS_COMMON = "vocabularyitems_common";
    
    final Logger logger = LoggerFactory.getLogger(VocabularyResource.class);

	public VocabularyResource() {
		super(VocabulariesCommon.class, VocabularyResource.class,
				VOCABULARIES_COMMON, VOCABULARYITEMS_COMMON);
	}

    @Override
    public String getServiceName() {
        return vocabularyServiceName;
    }

    @Override
    public String getItemServiceName() {
        return vocabularyItemServiceName;
    }
    
	@Override
	public Class<VocabulariesCommon> getCommonPartClass() {
		return VocabulariesCommon.class;
	}

    /**
     * @return the name of the property used to specify references for items in this type of
     * authority. For most authorities, it is ServiceBindingUtils.AUTH_REF_PROP ("authRef").
     * Some types (like Vocabulary) use a separate property.
     */
	@Override
    protected String getRefPropName() {
    	return ServiceBindingUtils.TERM_REF_PROP;
    }
	
	@Override
	protected String getOrderByField(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
		String result = null;

		result = authorityItemCommonSchemaName + ":" + VocabularyItemJAXBSchema.DISPLAY_NAME;

		return result;
	}
	
	@Override
	protected String getPartialTermMatchField(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
		return getOrderByField(ctx);
	}

	/*
	 * The item schema for the Vocabulary service does not support a multi-valued term list.  Only authorities that support
	 * term lists need to implement this method.
	 */
	@Override
	public String getItemTermInfoGroupXPathBase() {
		return null;
	}
}
