 /*	
 * IDPattern
 *
 * Models an identifier (ID), which consists of multiple IDParts.
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

package org.collectionspace.services;

public interface IDPatternJAXBSchema {
	final static String ID_PATTERN_NAME = "idPatternName";
	final static String CSID = "csid";
	final static String URI = "uri";
	// Insert other fields here, corresponding to the schema declarations in id.xsd.
}


