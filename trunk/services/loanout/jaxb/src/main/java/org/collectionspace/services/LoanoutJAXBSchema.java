/**
 * 
 */
package org.collectionspace.services;

public interface LoanoutJAXBSchema {

    final static String LOAN_OUT_NUMBER = "loanOutNumber";
    final static String BORROWER = "borrower";
    final static String BORROWERS_CONTACT = "borrowersContact";
    final static String LENDERS_AUTHORIZER = "lendersAuthorizer";
    final static String LENDERS_AUTHORIZATION_DATE = "lendersAuthorizationDate";
    final static String LENDERS_CONTACT = "lendersContact";
    
    /*
     * Omitting loaned object status fields in release 0.5.2,
     * as these are likely to be repeatable or else
     * handled in some alternate way in release 0.7.
     */
    // final static String LOANED_OBJECT_STATUS = "loanedObjectStatus";
    // final static String LOANED_OBJECT_STATUS_DATE = "loanedObjectStatusDate";
    // final static String LOANED_OBJECT_STATUS_NOTE = "loanedObjectStatusNote";
               
    final static String LOAN_OUT_DATE = "loanOutDate";
    final static String LOAN_RETURN_DATE = "loanReturnDate";
    final static String LOAN_RENEWAL_APPLICATION_DATE = "loanRenewalApplicationDate";
    final static String SPECIAL_CONDITIONS_OF_LOAN = "specialConditionsOfLoan";
    final static String LOAN_OUT_NOTE = "loanOutNote";
    final static String LOAN_PURPOSE = "loanPurpose";
}
