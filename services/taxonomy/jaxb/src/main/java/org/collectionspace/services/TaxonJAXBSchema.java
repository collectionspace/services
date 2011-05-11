/**
 * 
 */
package org.collectionspace.services;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;

public interface TaxonJAXBSchema extends AuthorityItemJAXBSchema {
	final static String TAXON_COMMON = "taxon_common";
	final static String NAME = "taxonFullName";
	final static String TAXON_RANK = "taxonRank";
        final static String TAXON_CURRENCY = "taxonCurrency";
        final static String TAXON_YEAR = "taxonYear";
        final static String TAXONOMIC_STATUS = "taxonomicStatus";
        final static String TAXON_IS_NAMED_HYBRID = "taxonIsNamedHybrid";

}

