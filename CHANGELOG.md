# CollectionSpace Services Changelog

## 8.3.2

### Bug Fixes

* Fix Nuxeo embedded server failing to start (ZipException / missing hibernate-core) by pointing the nuxeo-server module at the cspace S3 Maven mirror instead of the legacy maven-eu.nuxeo.org repo

## 8.3.0

* Add new endpoint for returning search results
* Add JDBC_URL_OPTS build parameter
* Add mapping for ElasticSearch imagine ordering
* Add CSV Authority export parameter
* Update notice of inventory completion report
* Update notice of intent to repatriate report
* Update full object with place details report
* Add contentPlaces, materialTechniqueDescription to ES mappings
* Add blob alt text to media responses
* Remove cspace_english namespace search
* Media Handling: Remove any query parameters from file name created, after uploading external media files 

### Bug Fixes

* Remove flickr urls from tests
