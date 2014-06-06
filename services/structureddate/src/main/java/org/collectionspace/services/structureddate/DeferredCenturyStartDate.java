package org.collectionspace.services.structureddate;


/**
 * A deferred date that represents the start of a century. The start year
 * can not be determined until the era of the century is known. Once the 
 * era is known, resolveDate() may be called to calculate the year.
 */
public class DeferredCenturyStartDate extends DeferredCenturyDate {

	public DeferredCenturyStartDate(int century) {
		super(century);
	}
	
	@Override
	public void resolveDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		Date startDate = DateUtils.getCenturyStartDate(century, era);
		
		setYear(startDate.getYear());
		setMonth(startDate.getMonth());
		setDay(startDate.getDay());
		setEra(startDate.getEra());
	}
}
