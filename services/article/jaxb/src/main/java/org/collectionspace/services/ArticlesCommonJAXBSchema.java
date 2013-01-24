/**
 * String constants to access XML element names of the ArticlesCommon class.
 */
package org.collectionspace.services;

/*
	<xs:element name="articles_common">
		<xs:complexType>
			<xs:sequence>
				<xs:element name="articleNumber" type="xs:string" />			<!-- An ID for the article (different than the CSID) -->
				<xs:element name="articleContentCsid" type="xs:string" />		<!-- The URL of the article's content -->
				<xs:element name="articleJobId" type="xs:string" />				<!-- The asynch job ID -if any -->
				<xs:element name="articleSource" type="xs:string" />			<!-- The name of the service/resource that was used to create the article. -->
				<xs:element name="articlePublisher" type="xs:string" />			<!-- The user who published the article -->
				<xs:element name="accessExpirationDate" type="xs:dateTime" />	<!-- When the article is no longer available for access -->
				<xs:element name="accessedCount" type="xs:integer" />			<!-- How many times the article has been accessed. -->
				<xs:element name="accessCountLimit" type="xs:integer" />		<!-- The maximum times the article can be accessed. -->
			</xs:sequence>
		</xs:complexType>
	</xs:element>
 */

public interface ArticlesCommonJAXBSchema {
    final static String ARTICLE_NUMBER = "articleNumber";
    final static String ARTICLE_CONTENT_NAME = "articleContentName";
    final static String ARTICLE_CONTENT_REPO_ID = "articleContentRepositoryId";
    final static String ARTICLE_CONTENT_URL = "articleContentUrl";
    final static String ARTICLE_JOB_ID = "articleJobId";
    final static String ARTICLE_SOURCE = "articleSource";
    final static String ARTICLE_ACCESS_EXPIRATION_DATE = "accessExpirationDate";
    final static String ARTICLE_ACCESSED_COUNT = "accessedCount";
    final static String ARTICLE_ACCESSED_COUNT_LIMIT = "accesseedCountLimit";
}