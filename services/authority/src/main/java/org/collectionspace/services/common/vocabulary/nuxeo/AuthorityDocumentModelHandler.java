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

import java.util.Map;

import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;

import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * AuthorityDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class AuthorityDocumentModelHandler<AuthCommon>
        extends DocHandlerBase<AuthCommon> {

    private String authorityCommonSchemaName;

    public AuthorityDocumentModelHandler(String authorityCommonSchemaName) {
        this.authorityCommonSchemaName = authorityCommonSchemaName;
    }

    /*
     * Non standard injection of CSID into common part, since caller may access through
     * shortId, and not know the CSID.
     * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#extractPart(org.nuxeo.ecm.core.api.DocumentModel, java.lang.String, org.collectionspace.services.common.service.ObjectPartType)
     */
    @Override
    protected Map<String, Object> extractPart(DocumentModel docModel, String schema, ObjectPartType partMeta)
            throws Exception {
        Map<String, Object> unQObjectProperties = super.extractPart(docModel, schema, partMeta);

        // Add the CSID to the common part
        if (partMeta.getLabel().equalsIgnoreCase(authorityCommonSchemaName)) {
            String csid = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
            unQObjectProperties.put("csid", csid);
        }

        return unQObjectProperties;
    }

    @Override
    public void handleCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        super.handleCreate(wrapDoc);
        // CSPACE-3178:
        // Uncomment once debugged and App layer is read to integrate
        // Experimenting with this uncommented now ...
        updateRefnameForAuthority(wrapDoc, authorityCommonSchemaName);//CSPACE-3178
    }

    protected void updateRefnameForAuthority(DocumentWrapper<DocumentModel> wrapDoc, String schemaName) throws Exception {
        DocumentModel docModel = wrapDoc.getWrappedObject();
        String suppliedRefName = (String) docModel.getProperty(schemaName, AuthorityJAXBSchema.REF_NAME);
        // CSPACE-3178:
        // Temporarily accept client-supplied refName values, rather than always generating such values,
        // Remove the surrounding 'if' statement when clients should no longer supply refName values.
        if (suppliedRefName == null || suppliedRefName.isEmpty()) {
            String shortIdentifier = (String) docModel.getProperty(schemaName, AuthorityJAXBSchema.SHORT_IDENTIFIER);
            String displayName = (String) docModel.getProperty(schemaName, AuthorityJAXBSchema.DISPLAY_NAME);
            MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
            RefName.Authority authority = RefName.buildAuthority(ctx.getTenantName(),
                    ctx.getServiceName(),
                    shortIdentifier,
                    displayName);
            String refName = authority.toString();
            docModel.setProperty(schemaName, AuthorityJAXBSchema.REF_NAME, refName);
        }
    }

    public String getShortIdentifier(DocumentWrapper<DocumentModel> wrapDoc, String schemaName) {
        DocumentModel docModel = wrapDoc.getWrappedObject();
        String shortIdentifier = null;
        try {
            shortIdentifier = (String) docModel.getProperty(schemaName, AuthorityJAXBSchema.SHORT_IDENTIFIER);
        } catch (ClientException ce) {
            throw new RuntimeException("AuthorityDocHandler Internal Error: cannot get shortId!", ce);
        }
        return shortIdentifier;
    }

    /**
     * Filters out selected values supplied in an update request.
     * 
     * @param objectProps the properties filtered out from the update payload
     * @param partMeta metadata for the object to fill
     */
    @Override
    public void filterReadOnlyPropertiesForPart(
            Map<String, Object> objectProps, ObjectPartType partMeta) {
        super.filterReadOnlyPropertiesForPart(objectProps, partMeta);
        String commonPartLabel = getServiceContext().getCommonPartLabel();
        if (partMeta.getLabel().equalsIgnoreCase(commonPartLabel)) {
            objectProps.remove(AuthorityJAXBSchema.CSID);
            objectProps.remove(AuthorityJAXBSchema.SHORT_IDENTIFIER);
            // Enable when clients should no longer supply refName values
            // objectProps.remove(AuthorityItemJAXBSchema.REF_NAME); // CSPACE-3178

        }
    }
}
