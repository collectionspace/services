/**
 * 
 */
package org.collectionspace.services;

import org.collectionspace.services.jaxb.InvocableJAXBSchema;

public interface BatchJAXBSchema extends InvocableJAXBSchema {
    final static String BATCH_NAME = "name";
    final static String BATCH_NOTES = "notes";
    final static String BATCH_CREATES_NEW_FOCUS = "createsNewFocus";
    final static String BATCH_CLASS_NAME = "className";
}
