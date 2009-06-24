 /*	
 * AlphabeticIDPart
 *
 * Models a part of an identifier (ID) whose values are an alphabetic series.
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

// @TODO: Add Javadoc comments

package org.collectionspace.services.id;

public class AlphabeticIDPart extends IDPart {

	public AlphabeticIDPart() {
		super(new AlphabeticIDGenerator());
	};

	public AlphabeticIDPart(String baseVal) {
		super(new AlphabeticIDGenerator(baseVal));
	};

	public AlphabeticIDPart(String startVal, String endVal, String baseVal) {
		super(new AlphabeticIDGenerator(startVal, endVal, baseVal));
	};
			
}
