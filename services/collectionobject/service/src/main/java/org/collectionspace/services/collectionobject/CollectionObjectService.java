/**
 * 
 */
package org.collectionspace.services.collectionobject;

import java.io.IOException;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import org.collectionspace.services.collectionobject.CollectionObject;

/**
 * @author remillet
 * 
 */
public interface CollectionObjectService {

	public final static String CO_SCHEMA_NAME = "collectionobject";

	// Create
	Document postCollectionObject(CollectionObject co)
			throws DocumentException, IOException;

	// Read single object
	Document getCollectionObject(String csid) throws DocumentException,
			IOException;

	// Read a list of objects
	Document getCollectionObjectList() throws DocumentException, IOException;

	// Update
	Document putCollectionObject(String csid, CollectionObject theUpdate)
			throws DocumentException, IOException;

	// Delete
	Document deleteCollectionObject(String csid) throws DocumentException,
			IOException;
}
