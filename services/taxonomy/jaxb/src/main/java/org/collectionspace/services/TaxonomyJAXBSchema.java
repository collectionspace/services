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
	final static String FULL_NAME = "fullName";
	final static String TAXON_RANK = "taxonRank";

}

