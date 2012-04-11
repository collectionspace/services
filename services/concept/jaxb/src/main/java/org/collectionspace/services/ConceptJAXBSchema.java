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
	final static String CONCEPT_RECORD_TYPE = "recordType";
	final static String CONCEPT_SCOPE_NOTE = "scopeNote";
	final static String CONCEPT_SCOPE_NOTE_SOURCE = "scopeNoteSource";
	final static String CONCEPT_SCOPE_NOTE_SOURCE_DETAIL = "scopeNoteSourceDetail";

	final static String CONCEPT_CITATION_GROUP_LIST = "citationGroupList";
	final static String CONCEPT_ADDL_SOURCE_GROUP_LIST = "additionalSourceGroupList";
	
	final static String CONCEPT_TERM_GROUP_TERM_TYPE = "termType";
	final static String CONCEPT_TERM_GROUP_TERM_QUALIFIER = "qualifier";
	final static String CONCEPT_TERM_GROUP_TERM_LANGUAGE = "termLanguage";
	final static String CONCEPT_TERM_GROUP_HISTORIC_FLAG = "historicFlag";

	final static String CONCEPT_ADDL_SOURCE_GROUP_SOURCE = "additionalSource";
	final static String CONCEPT_ADDL_SOURCE_GROUP_DETAIL = "additionalSourceDetail";
	final static String CONCEPT_ADDL_SOURCE_GROUP_UID = "additionalSourceUID";
}

