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
     * Retrieves the list of object type identifiers known by this service. Example: cluInfo.
     * @return list of object type identifiers
     * @throws OperationFailedException unable to complete request
	 */
    public List<String> findObjectTypes() throws OperationFailedException;

    /** 
     * Retrieves the basic dictionary information about a particular object structure. Including all variations based on a certain type and state. Example: Given that a CLU is of type "Course" and in the state of "Proposed," tell which fields are read only, mandatory, not applicable, have enumerations available, etc.
     * @param objectTypeKey identifier of the object type
     * @return describes the fields for the input object type
     * @throws DoesNotExistException specified objectTypeKey not found
     * @throws InvalidParameterException invalid objectTypeKey
     * @throws MissingParameterException missing objectTypeKey
     * @throws OperationFailedException unable to complete request
	 */
    public ObjectStructure fetchObjectStructure(@WebParam(name="objectTypeKey")String objectTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;

    /** 
     * Retrieves the list of enumeration values for a particular enumeration with a certain context for a particular date. The values returned should be those where the supplied date is between the effective and expiration dates. Certain enumerations may not support this functionality.
     * @param enumerationKey identifier of the enumeration
     * @param contextType identifier of the enumeration context type
     * @param contextValue value of the enumeration context
     * @param contextDate date and time to get the enumeration for
     * @return list of enumerated codes and values
     * @throws DoesNotExistException enumerationKey not found
     * @throws InvalidParameterException invalid enumerationKey, contextType, contextValue, contextDate
     * @throws MissingParameterException missing enumerationKey, contextType, contextValue, contextDate
     * @throws OperationFailedException unable to complete request
	 */
    public List<EnumeratedValue> fetchEnumeration(@WebParam(name="enumerationKey")String enumerationKey, @WebParam(name="contextType")String contextType, @WebParam(name="contextValue")String contextValue, @WebParam(name="contextDate")Date contextDate) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;

    /** 
     * Retrieves the list of search types known by this service.
     * @return list of search type information
     * @throws OperationFailedException unable to complete request
	 */
    public List<SearchTypeInfo> findSearchTypes() throws OperationFailedException;

    /** 
     * Retrieves information about a particular search type.
     * @param searchTypeKey identifier of the search type
     * @return information on the search type
     * @throws DoesNotExistException specified searchTypeKey not found
     * @throws InvalidParameterException invalid searchTypeKey
     * @throws MissingParameterException searchTypeKey not specified
     * @throws OperationFailedException unable to complete request
	 */
    public SearchTypeInfo fetchSearchType(@WebParam(name="searchTypeKey")String searchTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;

    /** 
     * Retrieves the list of search types which return results in the specified format.
     * @param searchResultTypeKey identifier of the search result type
     * @return list of search type information
     * @throws DoesNotExistException specified searchResultTypeKey not found
     * @throws InvalidParameterException invalid searchResultTypeKey
     * @throws MissingParameterException searchResultTypeKey not specified
     * @throws OperationFailedException unable to complete request
	 */
    public List<SearchTypeInfo> findSearchTypesByResult(@WebParam(name="searchResultTypeKey")String searchResultTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;

    /** 
     * Retrieves the list of search types which use criteria in the specified format.
     * @param searchCriteriaTypeKey identifier of the search criteria
     * @return list of search type information
     * @throws DoesNotExistException specified searchCriteriaTypeKey not found
     * @throws InvalidParameterException invalid searchCriteriaTypeKey
     * @throws MissingParameterException searchCriteriaTypeKey not specified
     * @throws OperationFailedException unable to complete request
	 */
    public List<SearchTypeInfo> findSearchTypesByCriteria(@WebParam(name="searchCriteriaTypeKey")String searchCriteriaTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;

    /** 
     * Retrieves the list of search result types known by this service. Search result types describe the return structure for a search.
     * @return list of search result type information
     * @throws OperationFailedException unable to complete request
	 */
    public List<SearchResultTypeInfo> findSearchResultTypes() throws OperationFailedException;

    /** 
     * Retrieves information about a particular search result type. Search result types describe the return structure for a search.
     * @param searchResultTypeKey identifier of the search result type
     * @return information on the search result type
     * @throws DoesNotExistException specified searchResultTypeKey not found
     * @throws InvalidParameterException invalid searchResultTypeKey
     * @throws MissingParameterException searchResultTypeKey not specified
     * @throws OperationFailedException unable to complete request
	 */
    public SearchResultTypeInfo fetchSearchResultType(@WebParam(name="searchResultTypeKey")String searchResultTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;

    /** 
     * Retrieves the list of search criteria types known by this service.
     * @return list of search criteria type information
     * @throws OperationFailedException unable to complete request
	 */
    public List<SearchCriteriaTypeInfo> findSearchCriteriaTypes() throws OperationFailedException;

    /** 
     * Retrieves information about a particular search criteria type.
     * @param searchCriteriaTypeKey identifier of the search criteria type
     * @return information on the search criteria type
     * @throws DoesNotExistException specified searchCriteriaTypeKey not found
     * @throws InvalidParameterException invalid searchCriteriaTypeKey
     * @throws MissingParameterException searchCriteriaTypeKey not specified
     * @throws OperationFailedException unable to complete request
	 */
    public SearchCriteriaTypeInfo fetchSearchCriteriaType(@WebParam(name="searchCriteriaTypeKey")String searchCriteriaTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;

    /** 
     * Retrieve the list of authentication types known by this service
     * @return list of authentication types
     * @throws OperationFailedException unable to complete request
	 */
    public List<AuthenticationType> findAuthenticationTypes() throws OperationFailedException;

    /** 
     * Retrieves a list of Principals for a given Person
     * @param personId person identifier
     * @return list of principal ids for the specified person
     * @throws DisabledIdentifierException personId found but has been retired
     * @throws DoesNotExistException personId not found
     * @throws InvalidParameterException invalid personId
     * @throws MissingParameterException missing personId
     * @throws OperationFailedException unable to complete request
     * @throws PermissionDeniedException authorization failure
	 */
    public List<String> findPrincipalIdsByPerson(@WebParam(name="personId")String personId) throws DisabledIdentifierException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;

    /** 
     * Retrieves a list of Principals for a given System
     * @param systemId system identifier
     * @return list of principal ids for the given system
     * @throws DisabledIdentifierException systemId found but has been retired
     * @throws DoesNotExistException systemId not found
     * @throws InvalidParameterException invalid systemId
     * @throws MissingParameterException missing systemId
     * @throws OperationFailedException unable to complete request
     * @throws PermissionDeniedException authorization failure
	 */
    public List<String> findPrincipalIdsBySystem(@WebParam(name="systemId")String systemId) throws DisabledIdentifierException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;

    /** 
     * Retrieves a Person for a given Principal
     * @param principalId principal identifier
     * @return person identifier that matches the supplied criteria
     * @throws DisabledIdentifierException principalId found but has been retired
     * @throws DoesNotExistException principalId not found
     * @throws InvalidParameterException invalid principalId
     * @throws MissingParameterException missing principalId
     * @throws OperationFailedException unable to complete request
     * @throws PermissionDeniedException authorization failure
	 */
    public String fetchPersonIdByPrincipal(@WebParam(name="principalId")String principalId) throws DisabledIdentifierException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;

    /** 
     * Retrieves a System for a given Principal
     * @param principalId principal identifier
     * @return system identifier that matches the supplied criteria
     * @throws DisabledIdentifierException systemId found but has been retired
     * @throws DoesNotExistException systemId not found
     * @throws InvalidParameterException invalid principalId
     * @throws MissingParameterException missing principalId
     * @throws OperationFailedException unable to complete request
     * @throws PermissionDeniedException authorization failure
	 */
    public String fetchSystemByPrincipal(@WebParam(name="principalId")String principalId) throws DisabledIdentifierException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;

    /** 
     * Retrieves a Principal Type for a given Principal
     * @param principalId principal identifier
     * @return principal type that matches the supplied criteria
     * @throws DisabledIdentifierException principalId found but has been retired
     * @throws DoesNotExistException principalId not found
     * @throws InvalidParameterException invalid principalId
     * @throws MissingParameterException missing principalId
     * @throws OperationFailedException unable to complete request
     * @throws PermissionDeniedException authorization failure
	 */
    public PrincipalType fetchPrincipalTypeForPrincipal(@WebParam(name="principalId")String principalId) throws DisabledIdentifierException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;

    /** 
     * Retrieves a list of Principal Types for a given list of Principal Ids
     * @param principalIdList list of principal identifiers
     * @return List of principal types that match the supplied criteria
     * @throws DisabledIdentifierException One or more principalIds found but have been retired
     * @throws DoesNotExistException One or more principalIds not found
     * @throws InvalidParameterException invalid principalIdList
     * @throws MissingParameterException missing principalId
     * @throws OperationFailedException unable to complete request
     * @throws PermissionDeniedException authorization failure
	 */
    public List<PrincipalType> findPrincipalTypesByPrincipalIdList(@WebParam(name="principalIdList")List<String> principalIdList) throws DisabledIdentifierException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;

    /** 
     * Retrieves results in tabular form for the specified parameters.
     * @param searchTypeKey search identifier
     * @param queryParamValues list of values for search parameters
     * @return list of results from the query
     * @throws DoesNotExistException specified search type not found
     * @throws InvalidParameterException invalid searchTypeKey, queryParamValueList
     * @throws MissingParameterException searchTypeKey, queryParamValueList not specified
     * @throws OperationFailedException unable to complete request
     * @throws PermissionDeniedException authorization failure
	 */
    public List<Result> searchForResults(@WebParam(name="searchTypeKey")String searchTypeKey, @WebParam(name="queryParamValues")List<QueryParamValue> queryParamValues) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;

    /** 
     * Assigns a Principal to a Person
     * @param principalId principal identifier
     * @param personId person identifier
     * @return status of the operation
     * @throws AlreadyExistsException association already exists
     * @throws DisabledIdentifierException personId or principalId found but has been retired
     * @throws DoesNotExistException personId, principalId does not exist
     * @throws InvalidParameterException invalid personId, principalId
     * @throws MissingParameterException missing personId, principalId
     * @throws OperationFailedException unable to complete request
     * @throws PermissionDeniedException authorization failure
	 */
    public Status assignPrincipalToPerson(@WebParam(name="principalId")String principalId, @WebParam(name="personId")String personId) throws AlreadyExistsException, DisabledIdentifierException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;

    /** 
     * Assigns a Principal to a System
     * @param principalId principal identifier
     * @param systemId system identifier
     * @return status of the operation
     * @throws AlreadyExistsException association already exists
     * @throws DisabledIdentifierException principalId, systemId found but has been retired
     * @throws DoesNotExistException principalId, systemId does not exist
     * @throws InvalidParameterException invalid principalId, systemId
     * @throws MissingParameterException missing principalId, systemId
     * @throws OperationFailedException unable to complete request
     * @throws PermissionDeniedException authorization failure
	 */
    public Status assignPrincipalToSystem(@WebParam(name="principalId")String principalId, @WebParam(name="systemId")String systemId) throws AlreadyExistsException, DisabledIdentifierException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;

    /** 
     * Remove a Principal from a Person
     * @param principalId principal identifier
     * @param personId person identifier
     * @return status of the operation
     * @throws DisabledIdentifierException personId or principalId found but has been retired
     * @throws DoesNotExistException personId, principalId, association does not exist
     * @throws InvalidParameterException invalid personId, principalId
     * @throws MissingParameterException missing personId, principalId
     * @throws OperationFailedException unable to complete request
     * @throws PermissionDeniedException authorization failure
	 */
    public Status removePrincipalFromPerson(@WebParam(name="principalId")String principalId, @WebParam(name="personId")String personId) throws DisabledIdentifierException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;

    /** 
     * Remove a Principal from a System
     * @param principalId principal identifier
     * @param systemId system identifier
     * @return status of the operation
     * @throws DisabledIdentifierException principalId, systemId found but has been retired
     * @throws DoesNotExistException principalId, systemId, association does not exist
     * @throws InvalidParameterException invalid principalId, systemId
     * @throws MissingParameterException missing principalId, systemId
     * @throws OperationFailedException unable to complete request
     * @throws PermissionDeniedException authorization failure
	 */
    public Status removePrincipalFromSystem(@WebParam(name="principalId")String principalId, @WebParam(name="systemId")String systemId) throws DisabledIdentifierException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;

}