/**
 * 
 */
package org.collectionspace.services;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;

/**
 * @author pschmitz
 *
 */
public interface TaxonomyJAXBSchema extends AuthorityItemJAXBSchema {
	final static String TAXONOMY_COMMON = "taxonomy_common";
	final static String NAME = "fullName";
	final static String RANK = "taxonRank";

}

