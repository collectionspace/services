package org.collectionspace.services.structureddate;


/**
 * A deferred date that represents the start of a half century. The start year
 * can not be determined until the era of the century is known. Once the 
 * era is known, resolveDate() may be called to calculate the year.
 */
public class DeferredHalfCenturyStartDate extends DeferredHalfCenturyDate {

	public DeferredHalfCenturyStartDate(int century, int half) {
		super(century, half);
	}

	@Override
	public void resolveDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		Date startDate = DateUtils.getHalfCenturyStartDate(century, half, era);
		
		setYear(startDate.getYear());
		setMonth(startDate.getMonth());
		setDay(startDate.getDay());
		setEra(startDate.getEra());
	}
}
