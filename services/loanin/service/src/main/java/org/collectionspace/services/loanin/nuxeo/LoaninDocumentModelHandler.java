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

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.loanin.LoansinCommon;
import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;

/** LoaninDocumentModelHandler
 *  $LastChangedRevision$
 *  $LastChangedDate$
 */
public class LoaninDocumentModelHandler
        extends DocHandlerBase<LoansinCommon, AbstractCommonList> {

    private static DocHandlerBase.CommonListReflection clr;
    static {
        clr = new DocHandlerBase.CommonListReflection();
        clr.NuxeoSchemaName= "loanin";
        clr.SummaryFields = "loanInNumber|lenderList|loanReturnDate|uri|csid";
        clr.AbstractCommonListClassname = "org.collectionspace.services.loanin.LoansinCommonList";
        clr.CommonListItemClassname = "org.collectionspace.services.loanin.LoansinCommonList$LoaninListItem";
        clr.ListItemMethodName = "getLoaninListItem";
        //ListItemsArray array elements: SETTER=0, ELEMENT=1, CONTAINER=2, SUBELEMENT=3;
        clr.ListItemsArray =   new String[][] { {"setLoanInNumber",   "loanInNumber",   "", ""},
                                                {"setLender",     "lenderList",     "lenderGroupList", "lender"},
                                                {"setLoanReturnDate", "loanReturnDate", "", ""}
                                              };
    }
    public DocHandlerBase.CommonListReflection getCommonListReflection(){
        return clr;
    }

}

