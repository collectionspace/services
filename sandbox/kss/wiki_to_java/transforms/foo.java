/*
 * Copyright 2008 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 public interface foo {
    /** 
     * Retrieves the list of communication media types that are supported for a given usage type.
     * @param commUsageTypeKey usage type identifier
     * @return list of communication media type information
     * @throws DoesNotExistException commUsageTypeKey not found
     * @throws InvalidParameterException invalid commUsageTypeKey
     * @throws MissingParameterException missing commUsageTypeKey
     * @throws OperationFailedException unable to complete request
	 */
    public List<CommMediaTypeInfo> getCommMediaTypesForUsageType(@WebParam(name="commUsageTypeKey")String commUsageTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;

}