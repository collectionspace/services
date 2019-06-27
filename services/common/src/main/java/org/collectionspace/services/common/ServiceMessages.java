/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2010 University of California at Berkeley

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
package org.collectionspace.services.common;

/**
 * Common service messages
 * @author
 */
public class ServiceMessages {

    private static final String FAILED = " failed: ";
    private static final String UNSUPPORTED = " unsupported: ";

    public static final String POST_FAILED = "POST request" + FAILED;
    public static final String GET_FAILED = "GET request" + FAILED;
    public static final String PUT_FAILED = "PUT request" + FAILED;
    
    public static final String CREATE_FAILED = "Create request" + FAILED;
    public static final String READ_FAILED = "Read request" + FAILED;
    public static final String REINDEX_FAILED = "Reindex request" + FAILED;
    public static final String UPDATE_FAILED = "Update request" + FAILED;
    
    public static final String POST_UNSUPPORTED = "POST request" + UNSUPPORTED;
    public static final String GET_UNSUPPORTED = "GET request" + UNSUPPORTED;
    public static final String GET_LIST_UNSUPPORTED = "GET/LIST request" + UNSUPPORTED;
    public static final String PUT_UNSUPPORTED = "PUT request" + UNSUPPORTED;
    public static final String DELETE_UNSUPPORTED = "DELETE request" + UNSUPPORTED;

    public static final String DELETE_FAILED = "Delete request" + FAILED;
    public static final String LIST_FAILED = "List request" + FAILED;
    public static final String SEARCH_FAILED = "Search request" + FAILED;
    public static final String AUTH_REFS_FAILED = "Authority references request" + FAILED;
    public static final String LIST_MIMETYPES_FAILED = "Could not get list of support MIME types for report outputs.";
    
    public static final String PASSWORD_RESET_REQUEST_FAILED = "Password reset request" + FAILED;
    public static final String UNKNOWN_ERROR_MSG = "Unknown error ";
    public static final String VALIDATION_FAILURE = "Validation failure ";
    public static final String MISSING_CSID = "missing csid";
    public static final String MISSING_INVALID_CSID = "missing/invalid csid=";
	public static final String SYNC_FAILED = "Synchonization failed: ";

    public static String resourceNotFoundMsg(String csid) {
        return String.format("The resource identified by CSID '%s' was not found.", csid);
    }

    public static String resourceNotReindexedMsg(String csid) {
        return String.format("The resource identified by CSID '%s' could not be reindexed. See the service logs for details.", csid);
    }
    
    public static String resourceTypeNotReindexedMsg(String resourceType) {
        return String.format("The resource types identified by '%s' could not be reindexed. See the service logs for details.", resourceType);
    }    

}
