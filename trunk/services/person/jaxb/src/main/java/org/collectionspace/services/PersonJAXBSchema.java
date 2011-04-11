/**
 * 
 */
package org.collectionspace.services;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;

/**
 * @author pschmitz
 *
 */
public interface PersonJAXBSchema extends AuthorityItemJAXBSchema {
	final static String PERSONS_COMMON = "persons_common";
	final static String FORE_NAME = "foreName";
	final static String MIDDLE_NAME = "middleName";
	final static String SUR_NAME = "surName";
	final static String INITIALS = "initials";
	final static String SALUTATIONS = "salutations";
	final static String TITLE = "title";
	final static String NAME_ADDITIONS = "nameAdditions";
	final static String BIRTH_DATE = "birthDate";
	final static String DEATH_DATE = "deathDate";
	final static String BIRTH_PLACE = "birthPlace";
	final static String DEATH_PLACE = "deathPlace";
	final static String GROUPS = "groups";
	final static String NATIONALITIES = "nationalities";
	final static String GENDER = "gender";
	final static String OCCUPATIONS = "occupations";
	final static String SCHOOLS_OR_STYLES = "schoolsOrStyles";
	final static String BIO_NOTE = "bioNote";
	final static String NAME_NOTE = "nameNote";
}

