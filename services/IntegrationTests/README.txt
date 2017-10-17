To See XMLReplay test reports, look in the source directory: ..\services\IntegrationTests\target\xml-replay-reports\index.xml-replay-master.xml.html

### To run a subset of XMLReplay tests

Modify the dev-master.xml file to include just the tests you are debugging:
	..\services\services\IntegrationTests\src\test\resources\test-data\xmlreplay\dev-master.xml

Then run this command:

	mvn test -Dtest=XmlReplayDevTest -Dmaven.surefire.debug=true

Notes on Using XMLReplay with CollectionSpace Services - http://wiki.collectionspace.org/display/~remillet/Notes+on+Using+XMLReplay+with+CollectionSpace+Services

