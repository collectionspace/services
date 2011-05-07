/**
 * 
 */
package org.collectionspace.services;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;

public interface TaxonomyJAXBSchema extends AuthorityItemJAXBSchema {
	final static String TAXON_COMMON = "taxon_common";
	final static String NAME = "taxonFullName";
	final static String RANK = "taxonRank";

}

