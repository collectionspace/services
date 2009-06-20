 /*	
 * SeriesIDPart
 *
 * Models a part of an identifier (ID) whose values are part of a series,
 * such as (for instance) an incrementing numeric or alphabetic value.
 * Values begin at an initial (base) value
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

public abstract class SeriesIDPart extends IDPart {

	protected String baseValue = "";
	protected String lastIdGenerated = null;
	
	public SeriesIDPart(StringIdentifierGenerator idGenerator, String baseVal) {
		// Store the identifier generator and base value,
		// and set the current value to the base value
		super(idGenerator);
		setBaseValue(baseVal);
	}

	// Store the base value
	protected void setBaseValue(String baseVal) {
		// @TODO: Throw an Exception if the base value is null.
		if (baseVal != null) {
			baseValue = baseVal;
		}
	};

	// Get the base value
	public String getBaseValue() {
		return baseValue;
	};

	// Get the next identifier in series
	public String nextIdentifier() {
		// @TODO: Add Exception-handling here ...
		// If no identifier has ever been generated,
		// or if the value of the last identifier was reset,
		// return the base value.
		if (lastIdGenerated == null) {
			lastIdGenerated = baseValue;
			return lastIdGenerated;
		// Otherwise, return the next value in the series.
		} else {
			lastIdGenerated = generator.nextStringIdentifier();
			return lastIdGenerated;
		}
	}

	// Reset the value of the last identifier generated.
	public void reset() {
		lastIdGenerated = null;
	};
	 
}
