/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.collectionspace.services.IntegrationTests.xmlreplay;

import org.apache.commons.httpclient.Header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class ServiceResult {
    public String testID = "";
    public String testGroupID = "";
    public String fullURL = "";
    public String deleteURL = "";
    public String location = "";
    public String CSID = "";
    public String subresourceCSID = "";
    public String requestPayload = "";
    public String result = "";
    public int responseCode = 0;
    public String responseMessage = "";
    public String method = "";
    public String error = "";
    public String fromTestID = "";
    public String auth = "";
    public String boundary = "";
    public String payloadStrictness = "";
    public long contentLength = 0;
    public String failureReason = "";
    public Header[] responseHeaders = new Header[0];
    public List<Integer> expectedCodes = new ArrayList<Integer>();
    private Map<String, TreeWalkResults> partSummaries = new HashMap<String, TreeWalkResults>();
    public void addPartSummary(String label, TreeWalkResults list){
        partSummaries.put(label, list);
    }
    public String partsSummary(boolean detailed){
        StringBuffer buf = new StringBuffer();
        if (!isDomWalkOK()){
            if (detailed) buf.append("\r\nDOM CHECK FAILED:\r\n");
            else buf.append("; DOM CHECK FAILED:");
        }
        for (Map.Entry<String,TreeWalkResults> entry : partSummaries.entrySet()) {
            String key = entry.getKey();
            TreeWalkResults value = entry.getValue();
            buf.append(" label:"+key+": ");
            if (detailed){
                buf.append("\r\n");
                buf.append(value.fullSummary());
            } else {
                buf.append(value.miniSummary());
            }

        }
        return buf.toString();
    }
    public boolean codeInSuccessRange(int code){
        if (0<=code && code<200){
            return false;
        } else if (400<=code) {
            return false;
        }
        return true;
    }

    public boolean isDomWalkOK(){
        if (Tools.isEmpty(payloadStrictness)){
            return true;
        }
        PAYLOAD_STRICTNESS strictness = PAYLOAD_STRICTNESS.valueOf(payloadStrictness);
        for (Map.Entry<String,TreeWalkResults> entry : partSummaries.entrySet()) {
            String key = entry.getKey();
            TreeWalkResults value = entry.getValue();
            if (value.hasDocErrors()){
                failureReason = " : DOM DOC_ERROR; ";
                return false;
            }
            switch (strictness){
            case STRICT:
                if (!value.isStrictMatch()) {
                    failureReason = " : DOM NOT STRICT; ";
                    return false;
                }
                break;
            case ADDOK:
                if (value.countFor(TreeWalkResults.TreeWalkEntry.STATUS.TEXT_DIFFERENT)>0) {
                    failureReason = " : DOM TEXT_DIFFERENT; ";
                    return false;
                }
                if (value.countFor(TreeWalkResults.TreeWalkEntry.STATUS.R_MISSING)>0){
                    failureReason = " : DOM R_MISSING; ";
                    return false;
                }
                break;
            case TEXT:
                if (value.countFor(TreeWalkResults.TreeWalkEntry.STATUS.TEXT_DIFFERENT)>0) {
                    failureReason = " : DOM TEXT_DIFFERENT; ";
                    return false;
                }
                break;
            case TREE:
                if (!value.treesMatch()) {
                    failureReason = " : DOM TREE MISMATCH; ";
                    return false;
                }
                break;
            case ZERO:
                break;
            }
        }
        return true;
    }

    public boolean gotExpectedResult(){
        if (Tools.notEmpty(failureReason)){
            return false;
        }
        for (Integer oneExpected : expectedCodes){
            if (responseCode == oneExpected){
                return isDomWalkOK();
            }
        }
        if ( expectedCodes.size()>0 && codeInSuccessRange(responseCode)){ //none found, but result expected.
            for (Integer oneExpected : expectedCodes){
                if ( ! codeInSuccessRange(oneExpected)){
                    return isDomWalkOK();
                }
            }
        }
        boolean ok = codeInSuccessRange(responseCode);
        if (ok) {
            return isDomWalkOK();
        }
        failureReason = " : STATUS CODE UNEXPECTED; ";
        return false;
    }

    //public static final String[] DUMP_OPTIONS = {"minimal", "detailed", "full"};
    public static enum DUMP_OPTIONS {minimal, detailed, full};

    public static enum PAYLOAD_STRICTNESS {ZERO, ADDOK, TREE, TEXT, STRICT};

    public String toString(){
        return detail(true);

    }
    public String detail(boolean includePayloads){
        String res =  "{"
                + ( gotExpectedResult() ? "SUCCESS" : "FAILURE"  )
                + failureReason
                +"; "+method
                +"; "+responseCode
                + ( (expectedCodes.size()>0) ? "; expectedCodes:"+expectedCodes : "" )
                + ( Tools.notEmpty(testID) ? "; testID:"+testID : "" )
                + ( Tools.notEmpty(testGroupID) ? "; testGroupID:"+testGroupID : "" )
                + ( Tools.notEmpty(fromTestID) ? "; fromTestID:"+fromTestID : "" )
                + ( Tools.notEmpty(responseMessage) ? "; msg:"+responseMessage : "" )
                +"; URL:"+fullURL
                +"; auth: "+auth
                + ( Tools.notEmpty(deleteURL) ? "; deleteURL:"+deleteURL : "" )
                + ( Tools.notEmpty(location) ? "; location.CSID:"+location : "" )
                + ( Tools.notEmpty(error) ? "; ERROR:"+error : "" )
                + "; gotExpected:"+gotExpectedResult()
                + ( includePayloads && Tools.notEmpty(requestPayload) ? "; requestPayload:\r\n"+requestPayload+"\r\n" : "" )
                + ( includePayloads && Tools.notEmpty(result) ? "; result:"+result : "" )
                + ( partsSummary(true))
                +"}";
        return res;
    }
    public String minimal(){
        return "{"
                + ( gotExpectedResult() ? "SUCCESS" : "FAILURE"  )
                + failureReason
                + ( Tools.notEmpty(testID) ? "; "+testID : "" )
                +"; "+method
                +"; "+responseCode
                + (expectedCodes.size()>0 ? "; expected:"+expectedCodes : "")
                + ( Tools.notEmpty(responseMessage) ? "; msg:"+responseMessage : "" )
                +"; URL:"+fullURL
                +"; auth: "+auth
                + ( Tools.notEmpty(error) ? "; ERROR:"+error : "" )
                + ( partsSummary(false))
                +"}";
    }
    public String dump(ServiceResult.DUMP_OPTIONS opt){
        switch (opt){
            case minimal:
                return minimal();
            case detailed:
                return detail(false);
            case full:
                return detail(true);
            default:
                return toString();
        }
    }
}
