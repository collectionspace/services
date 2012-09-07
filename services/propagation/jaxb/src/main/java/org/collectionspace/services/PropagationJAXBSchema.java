/**
 * 
 */
package org.collectionspace.services;

public interface PropagationJAXBSchema {
    final static String LOAN_IN_NUMBER = "propagationNumber";
    // Note: LENDERS variable is of type org.collectionspace.services.propagation.LenderList
    final static String LENDERS = "lenders";
    final static String LENDERS_AUTHORIZER = "lendersAuthorizer";
    final static String LENDERS_AUTHORIZATION_DATE = "lendersAuthorizationDate";
    final static String LENDERS_CONTACT = "lendersContact";
    final static String LOAN_IN_CONTACT = "propagationContact";
    final static String LOAN_IN_CONDITIONS = "propagationConditions";
    final static String LOAN_IN_DATE = "propagationDate";
    final static String LOAN_RETURN_DATE = "loanReturnDate";
    final static String LOAN_RENEWAL_APPLICATION_DATE = "loanRenewalApplicationDate";
    final static String LOAN_IN_NOTE = "propagationNote";
    final static String LOAN_PURPOSE = "loanPurpose";
}


