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
package org.collectionspace.services.common.vocabulary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentUtils;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

/**
 * RefNameServiceUtils is a collection of services utilities related to refName usage.
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class RefNameServiceUtils {

    private final Logger logger = LoggerFactory.getLogger(RefNameServiceUtils.class);

    public static AuthorityRefDocList getAuthorityRefDocs(ServiceContext ctx,
            RepositoryClient repoClient,
            String serviceType,
            String refName,
            int pageSize, int pageNum, boolean computeTotal) throws DocumentException, DocumentNotFoundException {
        AuthorityRefDocList wrapperList = new AuthorityRefDocList();
        List<AuthorityRefDocList.AuthorityRefDocItem> list =
                wrapperList.getAuthorityRefDocItem();
        TenantBindingConfigReaderImpl tReader =
                ServiceMain.getInstance().getTenantBindingConfigReader();
        List<ServiceBindingType> servicebindings = tReader.getServiceBindingsByType(ctx.getTenantId(), serviceType);
        if (servicebindings == null || servicebindings.isEmpty()) {
            return null;
        }
        // Need to escape the quotes in the refName
        // TODO What if they are already escaped?
        String escapedRefName = refName.replaceAll("'", "\\\\'");
//    	String domain = 
//    		tReader.getTenantBinding(ctx.getTenantId()).getRepositoryDomain();
        ArrayList<String> docTypes = new ArrayList<String>();
        HashMap<String, ServiceBindingType> queriedServiceBindings = new HashMap<String, ServiceBindingType>();
        HashMap<String, List<String>> authRefFieldsByService = new HashMap<String, List<String>>();
        StringBuilder whereClause = new StringBuilder();
        boolean fFirst = true;
        for (ServiceBindingType sb : servicebindings) {
            List<String> authRefFields =
                    ServiceBindingUtils.getAllPartsPropertyValues(sb,
                    ServiceBindingUtils.AUTH_REF_PROP, ServiceBindingUtils.QUALIFIED_PROP_NAMES);
            if (authRefFields.isEmpty()) {
                continue;
            }
            String fieldName = "";
            for (int i = 0; i < authRefFields.size(); i++) {
                // fieldName = DocumentUtils.getDescendantOrAncestor(authRefFields.get(i));
                fieldName = DocumentUtils.getAncestorAuthRefFieldName(authRefFields.get(i));
                authRefFields.set(i, fieldName);
            }

            String docType = sb.getObject().getName();
            queriedServiceBindings.put(docType, sb);
            authRefFieldsByService.put(docType, authRefFields);
            docTypes.add(docType);
            /*
            // HACK - need to get qualified properties from the ServiceBinding
            String prefix = "";
            if(docType.equalsIgnoreCase("Intake"))
            prefix = "intakes_common:";
            else if(docType.equalsIgnoreCase("Loanin"))
            prefix = "loansin_common:";
            else if(docType.equalsIgnoreCase("Acquisition"))
            prefix = "acquisitions_common:";
             */
            for (String field : authRefFields) {
                // Build up the where clause for each authRef field
                if (fFirst) {
                    fFirst = false;
                } else {
                    whereClause.append(" OR ");
                }
                //whereClause.append(prefix);
                whereClause.append(field);
                whereClause.append("='");
                whereClause.append(escapedRefName);
                whereClause.append("'");
            }
        }
        if (fFirst) // found no authRef fields - nothing to query
        {
            return wrapperList;
        }
        String fullQuery = whereClause.toString(); // for debug
        // Now we have to issue the search
        DocumentWrapper<DocumentModelList> docListWrapper = repoClient.findDocs(ctx,
                docTypes, whereClause.toString(), pageSize, pageNum, computeTotal);
        // Now we gather the info for each document into the list and return
        DocumentModelList docList = docListWrapper.getWrappedObject();
        Iterator<DocumentModel> iter = docList.iterator();
        while (iter.hasNext()) {
            DocumentModel docModel = iter.next();
            AuthorityRefDocList.AuthorityRefDocItem ilistItem = new AuthorityRefDocList.AuthorityRefDocItem();
            String csid = NuxeoUtils.extractId(docModel.getPathAsString());
            String docType = docModel.getDocumentType().getName();
            ServiceBindingType sb = queriedServiceBindings.get(docType);
            if (sb == null) {
                throw new RuntimeException(
                        "getAuthorityRefDocs: No Service Binding for docType: " + docType);
            }
            String serviceContextPath = "/" + sb.getName().toLowerCase() + "/";
            // The id and URI are the same on all doctypes
            ilistItem.setDocId(csid);
            ilistItem.setUri(serviceContextPath + csid);
            ilistItem.setDocType(docType);
            ilistItem.setDocNumber(
                    ServiceBindingUtils.getMappedFieldInDoc(sb, ServiceBindingUtils.OBJ_NUMBER_PROP, docModel));
            ilistItem.setDocName(
                    ServiceBindingUtils.getMappedFieldInDoc(sb, ServiceBindingUtils.OBJ_NAME_PROP, docModel));
            // Now, we have to loop over the authRefFieldsByService to figure
            // out which field matched this. Ignore multiple matches.
            List<String> authRefFields = authRefFieldsByService.get(docType);
            if (authRefFields == null || authRefFields.isEmpty()) {
                throw new RuntimeException(
                        "getAuthorityRefDocs: internal logic error: can't fetch authRefFields for DocType.");
            }
            boolean fRefFound = false;
            // Use this if we go to qualified field names
            for (String field : authRefFields) {
                String[] strings = field.split(":");
                if (strings.length != 2) {
                    throw new RuntimeException(
                            "getAuthorityRefDocs: Bad configuration of authRefField.");
                }
                try {
                    Object fieldValue = docModel.getProperty(strings[0], strings[1]);
                    fRefFound = refNameFoundInField(refName, fieldValue);
                    if (fRefFound) {
                        ilistItem.setSourceField(field);
                        // FIXME Returns only the first field in which the refName is found.
                        // We may want to return all; this may require multiple sourceFields
                        // in the list item schema.
                        break;
                    }

                } catch (ClientException ce) {
                    throw new RuntimeException(
                            "getAuthorityRefDocs: Problem fetching: " + field, ce);
                }
            }
            // Used before going to schema-qualified field names.
            /*
            for(String field:authRefFields){
            try {
            if(refName.equals(docModel.getPropertyValue(field))) {
            ilistItem.setSourceField(field);
            fRefFound = true;
            break;
            }
            } catch(ClientException ce) {
            throw new RuntimeException(
            "getAuthorityRefDocs: Problem fetching: "+field, ce);
            }
            }
             * 
             */
            if (!fRefFound) {
                throw new RuntimeException(
                        "getAuthorityRefDocs: Could not find refname in object:"
                        + docType + ":" + csid);
            }
            list.add(ilistItem);
        }
        return wrapperList;
    }

    /*
     * Identifies whether the refName was found in the supplied field.
     *
     * Only works for:
     * * Scalar fields
     * * Repeatable scalar fields (aka multi-valued fields)
     *
     * Does not work for:
     * * Structured fields (complexTypes)
     * * Repeatable structured fields (repeatable complexTypes)
     */
    private static boolean refNameFoundInField(String refName, Object fieldValue) {

        boolean result = false;
        if (fieldValue instanceof List) {
            List<String> fieldValueList = (List) fieldValue;
            for (String listItemValue : fieldValueList) {
                if (refName.equals(listItemValue)) {
                    result = true;
                    break;
                }

            }
        } else {
            if (refName.equals(fieldValue)) {
                result = true;
            }
        }
        return result;
    }
}

