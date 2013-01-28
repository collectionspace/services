/**
 * String constants to access XML element names of the ArticlesCommon class.
 */
package org.collectionspace.services;

/*
	<xs:element name="articles_common">
		<xs:complexType>
			<xs:sequence>
				<xs:element name="itemNumber" type="xs:string" />				<!-- An identifier for the article (different than the CSID) -->
				<xs:element name="itemContentName" type="xs:string" />			<!-- A name for the article's content  -->
				<xs:element name="itemContentId" type="xs:string" />			<!-- The backend repository ID of the article's content -->
				<xs:element name="itemContentUri" type="xs:string" />			<!-- The publicly accessible URL of the article's content -->	
				<xs:element name="itemJobId" type="xs:string" />				<!-- The asynch job ID -if any -->
				<xs:element name="itemSource" type="xs:string" />				<!-- The name of the service/resource that was used to create the article. -->
				<xs:element name="itemExpirationDate" type="xs:dateTime" />		<!-- When the article is no longer available for access -->
				<xs:element name="itemCount" type="xs:integer" />				<!-- How many times the article has been accessed. -->
				<xs:element name="itemCountLimit" type="xs:integer" />			<!-- The maximum times the article can be accessed. -->
			</xs:sequence>
		</xs:complexType>
	</xs:element>
 */

public interface PublicitemsCommonJAXBSchema {
    final static String ITEM_NUMBER = "itemNumber";
    final static String ITEM_CONTENT_NAME = "itemContentName";
    final static String ITEM_CONTENT_REPO_ID = "itemContentId";
    final static String ITEM_CONTENT_URI = "itemContentUri";
    final static String ITEM_JOB_ID = "itemJobId";
    final static String ITEM_SOURCE = "itemSource";
    final static String ITEM_ACCESS_EXPIRATION_DATE = "itemExpirationDate";
    final static String ITEM_ACCESSED_COUNT = "itemCount";
    final static String ITEM_ACCESSED_COUNT_LIMIT = "itemCountLimit";
}