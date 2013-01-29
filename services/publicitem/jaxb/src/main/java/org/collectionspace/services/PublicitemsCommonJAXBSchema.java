/**
 * String constants to access XML element names of the PublicitemsCommon class.
 */
package org.collectionspace.services;

/*
	<xs:element name="publicitems_common">
		<xs:complexType>
			<xs:sequence>
				<xs:element name="itemNumber" type="xs:string" />				<!-- An identifier for the publicitem (different than the CSID) -->
				<xs:element name="contentName" type="xs:string" />				<!-- A name for the publicitem's content  -->
				<xs:element name="contentId" type="xs:string" />				<!-- The the ID of the content blob in the back-end repository -->
				<xs:element name="contentUri" type="xs:string" />				<!-- The publicly accessible URL of the publicitem's content -->	
				<xs:element name="contentCreationJobId" type="xs:string" />		<!-- The asynch job ID -if any -->
				<xs:element name="contentSource" type="xs:string" />			<!-- The name of the service/resource that was used to create the publicitem. -->
				<xs:element name="contentExpirationDate" type="xs:dateTime" />	<!-- When the publicitem is no longer available for access -->
				<xs:element name="contentAccessedCount" type="xs:integer" />	<!-- How many times the publicitem has been accessed. -->
				<xs:element name="contentAccessCountLimit" type="xs:integer" />	<!-- The maximum times the publicitem can be accessed. -->
			</xs:sequence>
		</xs:complexType>
	</xs:element>
 */

public interface PublicitemsCommonJAXBSchema {
    final static String ITEM_NUMBER = "itemNumber";
    final static String ITEM_CONTENT_NAME = "contentName";
    final static String ITEM_CONTENT_REPO_ID = "contentId";
    final static String ITEM_CONTENT_URI = "contentUri";
    final static String ITEM_JOB_ID = "contentCreationJobId";
    final static String ITEM_SOURCE = "contentSource";
    final static String ITEM_ACCESS_EXPIRATION_DATE = "contentExpirationDate";
    final static String ITEM_ACCESSED_COUNT = "contentAccessedCount";
    final static String ITEM_ACCESSED_COUNT_LIMIT = "contentAccessCountLimit";
}