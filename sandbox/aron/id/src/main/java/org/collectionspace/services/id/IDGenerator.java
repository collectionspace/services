 /*	
 * IDGenerator
 *
 * Interface for a generator class that returns IDs.
 *
 * Copyright 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * @author $Author: aron $
 * @version $Revision: 267 $
 * $Date: 2009-06-19 19:03:38 -0700 (Fri, 19 Jun 2009) $
 */
 
package org.collectionspace.services.id;

public interface IDGenerator {
    
	public void reset();

	public String getInitialID();

	public String getCurrentID();

	public String getNextID();

	public boolean isValidID(String value);

	// public String getRegex();
		
}
