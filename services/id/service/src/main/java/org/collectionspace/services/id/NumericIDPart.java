 /*	
 * NumericIDPart
 *
 * Models a part of an identifier (ID) whose values consist of an
 * incrementing numeric series, with those values represented as
 * String objects.
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

public class NumericIDPart extends IDPart {

	public NumericIDPart() throws IllegalArgumentException {
		super(new NumericIDGenerator());
	};

	// Store the appropriate Numeric ID generator and the base value for this part.
	public NumericIDPart(String baseVal) throws IllegalArgumentException {
		super(new NumericIDGenerator(baseVal));
	};

	// Store the appropriate Numeric ID generator, and the base value
	// and maximum length for this part.
	public NumericIDPart(String baseVal, String maxLength) throws IllegalArgumentException {
		super(new NumericIDGenerator(baseVal, maxLength));
	};

}
