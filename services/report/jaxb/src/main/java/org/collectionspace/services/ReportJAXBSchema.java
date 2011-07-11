/**
 * 
 */
package org.collectionspace.services;

import org.collectionspace.services.jaxb.InvocableJAXBSchema;

/**
 * @author pschmitz
 *
 */
public interface ReportJAXBSchema extends InvocableJAXBSchema {
	final static String NAME = "name";
	final static String NOTES = "notes";
	final static String FILENAME = "filename";
	final static String OUTPUT_MIME = "outputMIME";
}


