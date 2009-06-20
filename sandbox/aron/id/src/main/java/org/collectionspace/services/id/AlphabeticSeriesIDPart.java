 /*	
 * AlphabeticSeriesIDPart
 *
 * Models a part of an identifier (ID) whose value is alphabetic,
 * and increments within a series of uppercase and/or lowercase values
 * in the USASCII character sequence.
 *
 * Copyright 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * @author $Author$
 * @version $Revision$
 * $Date$
 */
 
package org.collectionspace.services.id;

import org.apache.commons.id.StringIdentifierGenerator;

public class AlphabeticSeriesIDPart extends SeriesIDPart {

	public AlphabeticSeriesIDPart(String baseVal) {
		// Store the appropriate Alphabetic ID generator and the base value for this part
		// Value 'false' refers to the NO_WRAP behavior of the StringIdentifierGenerator.
		super(new AlphabeticGenerator(false, baseVal), baseVal);
	};
 
}
