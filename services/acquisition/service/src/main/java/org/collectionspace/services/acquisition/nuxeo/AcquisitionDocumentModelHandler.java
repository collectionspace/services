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
package org.collectionspace.services.acquisition.nuxeo;

import java.util.ArrayList;
import java.util.List;

import org.collectionspace.services.acquisition.AcquisitionsCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;
import org.collectionspace.services.common.service.DocHandlerParams;
import org.collectionspace.services.common.service.ListResultField;

/** AcquisitionDocumentModelHandler
 *  $LastChangedRevision: $
 *  $LastChangedDate: $
 */
public class AcquisitionDocumentModelHandler
        extends DocHandlerBase<AcquisitionsCommon, AbstractCommonList> {

    private static DocHandlerParams.Params params;
    static {
    	params = new DocHandlerParams.Params();
        params.setSchemaName("acquisition");
        params.setDublinCoreTitle("");//"CollectionSpace-Acquisition";
        params.setSummaryFields("acquisitionReferenceNumber|acquisitionSources|owners|uri|csid");
        params.setAbstractCommonListClassname("org.collectionspace.services.acquisition.AcquisitionsCommonList");
        params.setCommonListItemClassname("org.collectionspace.services.acquisition.AcquisitionsCommonList$AcquisitionListItem");
        params.setListResultsItemMethodName("getAcquisitionListItem");
        DocHandlerParams.Params.ListResultsFields lrfs = 
        	new DocHandlerParams.Params.ListResultsFields();
        params.setListResultsFields(lrfs);
        List<ListResultField> lrfl = lrfs.getListResultField();
		ListResultField lrf = new ListResultField();
		lrf.setSetter("setAcquisitionReferenceNumber");
		lrf.setXpath("acquisitionReferenceNumber");
		lrfl.add( lrf ); 
		lrf = new ListResultField();
		lrf.setSetter("setAcquisitionSource");
		lrf.setXpath("acquisitionSources/[0]");
		lrfl.add( lrf ); 
		lrf = new ListResultField();
		lrf.setSetter("setOwner");
		lrf.setXpath("owners/[0]");
		lrfl.add( lrf ); 
		/*
				{"setAcquisitionReferenceNumber","acquisitionReferenceNumber"},
				{"setAcquisitionSource","acquisitionSources/[0]"},
				{"setOwner","owners/[0]"}
  };					*/
    }
    public DocHandlerParams.Params getDocHandlerParams(){
        return params;
    }

}

