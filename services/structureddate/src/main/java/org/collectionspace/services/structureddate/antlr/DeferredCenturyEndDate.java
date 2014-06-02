package org.collectionspace.services.structureddate.antlr;

import org.collectionspace.services.structureddate.Date;
import org.collectionspace.services.structureddate.DateUtils;
import org.collectionspace.services.structureddate.Era;

/**
 * A deferred date that represents the end of a century. The end year
 * can not be determined until the era of the century is known. Once the 
 * era is known, finalizeDate() may be called to calculate the year.
 */
public class DeferredCenturyEndDate extends DeferredCenturyDate {

	public DeferredCenturyEndDate(Integer century) {
		super(century);
		
		setMonth(12);
		setDay(31);
	}
	
	@Override
	public void finalizeDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
			setEra(era);
		}
		
		setYear(DateUtils.getCenturyEndYear(century, era));
	}
}
