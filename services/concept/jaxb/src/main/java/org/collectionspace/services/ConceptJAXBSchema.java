/**
 * 
 */
package org.collectionspace.services;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;

/**
 * @author pschmitz
 *
 */
public interface ConceptJAXBSchema extends AuthorityItemJAXBSchema {
	final static String CONCEPTS_COMMON = "concepts_common";
	final static String CONCEPT_TYPE = "conceptType";
	final static String CONCEPT_SCOPE_NOTE = "scopeNote";
	final static String CONCEPT_SCOPE_NOTE_SOURCE = "scopeNoteSource";
	final static String CONCEPT_SCOPE_NOTE_SOURCE_DETAIL = "scopeNoteSourceDetail";
	final static String CONCEPT_REMARKS = "remarks";
	final static String CONCEPT_DISPLAY_TERM_FLAG = "displayTermFlag";

	final static String CONCEPT_TERM_GROUP_LIST = "conceptTermGroupList";
	final static String CONCEPT_CITATION_GROUP_LIST = "citationGroupList";
	final static String CONCEPT_ADDL_TERM_SOURCE_GROUP_LIST = "additionalTermSourceGroupList";
	
	final static String CONCEPT_TERM_GROUP_TERM = "term";
	final static String CONCEPT_TERM_GROUP_TERM_TYPE = "termType";
	final static String CONCEPT_TERM_GROUP_TERM_QUALIFIER = "termQualifier";
	final static String CONCEPT_TERM_GROUP_TERM_LANGUAGE = "termLanguage";
	final static String CONCEPT_TERM_GROUP_HISTORICAL_FLAG = "historicalFlag";
	final static String CONCEPT_TERM_GROUP_SOURCE = "source";
	final static String CONCEPT_TERM_GROUP_SOURCE_DETAIL = "sourceDetail";
	final static String CONCEPT_TERM_GROUP_SOURCE_UID = "sourceUID";
	final static String CONCEPT_TERM_GROUP_TERM_DATE = "termDate";
	
	final static String CONCEPT_CITATION_GROUP_SOURCE = "source";
	final static String CONCEPT_CITATION_GROUP_SOURCE_DETAIL = "sourceDetail";

	final static String CONCEPT_ADDL_TERM_SOURCE_GROUP_SOURCE = "source";
	final static String CONCEPT_ADDL_TERM_SOURCE_GROUP_DETAIL = "sourceDetail";
	final static String CONCEPT_ADDL_TERM_SOURCE_GROUP_UID = "sourceUID";
}

