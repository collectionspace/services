 /*	
 * AlphabeticIDPart
 *
 * Models a part of an identifier (ID) whose values consist of incrementing
 * alphabetic characters, from within a sequence of characters.
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * $LastChangedBy$
 * $LastChangedRevision$
 * $LastChangedDate$
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
