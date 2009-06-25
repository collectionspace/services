/*	
 * IDService
 *
 * Interface for the methods of the ID Service.
 *
 * Copyright 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Based on work by Richard Millet and Sanjay Dalal.
 *
 * @author $Author: aron $
 * @version $Revision: 267 $
 * $Date: 2009-06-19 19:03:38 -0700 (Fri, 19 Jun 2009) $
 */
 
package org.collectionspace.services;

import java.io.IOException;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import org.collectionspace.services.id.*;
import org.collectionspace.services.id.IDServiceNuxeoImpl;

public interface IDService {

	public final static String ID_SCHEMA_NAME = "id"; // Note to self: Where is this used?

/*
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
			
*/

}
