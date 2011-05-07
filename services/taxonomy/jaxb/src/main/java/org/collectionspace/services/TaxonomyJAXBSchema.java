/**
 * 
 */
package org.collectionspace.services;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;

public interface TaxonomyJAXBSchema extends AuthorityItemJAXBSchema {
	final static String TAXONOMY_COMMON = "taxonomy_common";
	final static String NAME = "taxonFullName";
	final static String RANK = "taxonRank";

}

