/**
 * 
 */
package org.collectionspace.services.acquisition;

import java.io.IOException;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import org.collectionspace.services.acquisition.Acquisition;

/**
 * @author remillet
 * 
 */
public interface AcquisitionService {

	public final static String ACQUISITION_SCHEMA_NAME = "acquisition";

	// Create
	Document postAcquisition(Acquisition co)
			throws DocumentException, IOException;

	// Read single object
	Document getAcquisition(String csid) throws DocumentException,
			IOException;

	// Read a list of objects
	Document getAcquisitionList() throws DocumentException, IOException;

	// Update
	Document putAcquisition(String csid, Acquisition theUpdate)
			throws DocumentException, IOException;

	// Delete
	Document deleteAcquisition(String csid) throws DocumentException,
			IOException;
}
