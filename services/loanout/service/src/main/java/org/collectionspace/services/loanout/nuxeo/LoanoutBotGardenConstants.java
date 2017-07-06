package org.collectionspace.services.loanout.nuxeo;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.LoanoutClient;

public class LoanoutBotGardenConstants {
	public static final String BOTGARDEN_PROFILE_NAME = CollectionSpaceClient.BOTGARDEN_PROFILE_NAME;
	public static final String BOTGARDEN_SCHEMA_NAME = LoanoutClient.SERVICE_NAME + CollectionSpaceClient.PART_LABEL_SEPARATOR + BOTGARDEN_PROFILE_NAME;

	public static final String LABEL_REQUESTED_SCHEMA_NAME = BOTGARDEN_SCHEMA_NAME;
	public static final String LABEL_REQUESTED_FIELD_NAME = "labelRequested";
	public static final String LABEL_REQUESTED_YES_VALUE = "Yes";
	public static final String LABEL_REQUESTED_NO_VALUE = "No";
	
	public static final String STYLED_NAME_SCHEMA_NAME = BOTGARDEN_SCHEMA_NAME;
	public static final String STYLED_NAME_FIELD_NAME = "styledName";
}
