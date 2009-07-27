/**
 * 
 */
package org.collectionspace.services.intake;

import java.io.IOException;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import org.collectionspace.services.intake.Intake;

/**
 * @author remillet
 * 
 */
public interface IntakeService {

	public final static String INTAKE_SCHEMA_NAME = "intake";

	// Create
	Document postIntake(Intake co)
			throws DocumentException, IOException;

	// Read single object
	Document getIntake(String csid) throws DocumentException,
			IOException;

	// Read a list of objects
	Document getIntakeList() throws DocumentException, IOException;

	// Update
	Document putIntake(String csid, Intake theUpdate)
			throws DocumentException, IOException;

	// Delete
	Document deleteIntake(String csid) throws DocumentException,
			IOException;
}
