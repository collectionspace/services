 /*	
 * YearIDGenerator
 *
 * Models a part of an identifier (ID) whose value is the current year
 * or a supplied year.
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

public class YearIDPart extends IDPart {

	public YearIDPart() {
		super(new YearIDGenerator());
	};

	public YearIDPart(String baseVal) {
		super(new YearIDGenerator(baseVal));
	};
		
}
