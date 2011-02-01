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
package org.collectionspace.services.loanin.nuxeo;

import java.util.ArrayList;
import java.util.List;

import org.collectionspace.services.common.service.ListResultField;
import org.collectionspace.services.common.service.DocHandlerParams;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.loanin.LoansinCommon;
import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;

/** LoaninDocumentModelHandler
 *  $LastChangedRevision$
 *  $LastChangedDate$
 */
public class LoaninDocumentModelHandler
        extends DocHandlerBase<LoansinCommon, AbstractCommonList> {

    private static DocHandlerParams.Params params;
    static {
    	params = new DocHandlerParams.Params();
        params.setSchemaName("loanin");
        params.setDublinCoreTitle("");//"CollectionSpace-Media";
        params.setSummaryFields("loanInNumber|lender|loanReturnDate|uri|csid");
        params.setAbstractCommonListClassname("org.collectionspace.services.loanin.LoansinCommonList");
        params.setCommonListItemClassname("org.collectionspace.services.loanin.LoansinCommonList$LoaninListItem");
        params.setListResultsItemMethodName("getLoaninListItem");
        DocHandlerParams.Params.ListResultsFields lrfs = 
        	new DocHandlerParams.Params.ListResultsFields();
        params.setListResultsFields(lrfs);
        List<ListResultField> lrfl = lrfs.getListResultField();
		ListResultField lrf = new ListResultField();
		lrf.setSetter("setLoanInNumber");
		lrf.setXpath("loanInNumber");
		lrfl.add( lrf ); 
		lrf = new ListResultField();
		lrf.setSetter("setLender");
		lrf.setXpath("lenderGroupList/[0]/lender");
		lrfl.add( lrf ); 
		lrf = new ListResultField();
		lrf.setSetter("setLoanReturnDate");
		lrf.setXpath("loanReturnDate");
		lrfl.add( lrf ); 
        /*
        //ListItemsArray array elements: SETTER=0, ELEMENT=1, CONTAINER=2, SUBELEMENT=3;
        clr.ListItemsArray =   new String[][] {
        		{"setLoanInNumber",   "loanInNumber"},
                {"setLender",     "lenderGroupList/[0]/lender"},
                {"setLoanReturnDate", "loanReturnDate"}
            };
         */
    }

    public DocHandlerParams.Params getDocHandlerParams(){
        return params;
    }

}

