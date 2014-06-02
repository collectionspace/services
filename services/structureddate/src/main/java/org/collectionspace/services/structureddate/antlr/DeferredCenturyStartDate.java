package org.collectionspace.services.structureddate.antlr;

import org.collectionspace.services.structureddate.Date;
import org.collectionspace.services.structureddate.DateUtils;
import org.collectionspace.services.structureddate.Era;

/**
 * A deferred date that represents the start of a century. The start year
 * can not be determined until the era of the century is known. Once the 
 * era is known, finalizeDate() may be called to calculate the year.
 */
public class DeferredCenturyStartDate extends DeferredCenturyDate {

	public DeferredCenturyStartDate(Integer century) {
		super(century);
		
		setMonth(1);
		setDay(1);
	}
	
	@Override
	public void finalizeDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
			setEra(era);
		}
		
		setYear(DateUtils.getCenturyStartYear(century, era));
	}
}
