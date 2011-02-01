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
package org.collectionspace.services.objectexit.nuxeo;

import org.collectionspace.services.common.service.ListResultField;
import org.collectionspace.services.common.service.DocHandlerParams;

import java.util.ArrayList;
import java.util.List;

import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;
import org.collectionspace.services.objectexit.ObjectexitCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;

public class ObjectExitDocumentModelHandler extends DocHandlerBase<ObjectexitCommon, AbstractCommonList> {
    private static DocHandlerParams.Params params;
    static {
    	params = new DocHandlerParams.Params();
        params.setSchemaName("objectexit");
        params.setDublinCoreTitle("");//"CollectionSpace-Media";
        params.setSummaryFields("exitNumber|currentOwner|uri|csid");
        params.setAbstractCommonListClassname("org.collectionspace.services.objectexit.ObjectexitCommonList");
        params.setCommonListItemClassname("org.collectionspace.services.objectexit.ObjectexitCommonList$ObjectexitListItem");
        params.setListResultsItemMethodName("getObjectexitListItem");
        DocHandlerParams.Params.ListResultsFields lrfs = 
        	new DocHandlerParams.Params.ListResultsFields();
        params.setListResultsFields(lrfs);
        List<ListResultField> lrfl = lrfs.getListResultField();
		ListResultField lrf = new ListResultField();
		lrf.setSetter("setExitNumber");
		lrf.setXpath("exitNumber");
		lrfl.add( lrf ); 
		lrf = new ListResultField();
		lrf.setSetter("setCurrentOwner");
		lrf.setXpath("currentOwner");
		lrfl.add( lrf ); 

        /*clr.ListItemsArray =   new String[][] {
						{"setExitNumber", "exitNumber"},
						{"setCurrentOwner", "currentOwner"}};
		*/
    }

    public DocHandlerParams.Params getDocHandlerParams(){
        return params;
    }
}

