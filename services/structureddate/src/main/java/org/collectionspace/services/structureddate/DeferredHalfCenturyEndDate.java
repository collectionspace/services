package org.collectionspace.services.structureddate;


/**
 * A deferred date that represents the end of a half century. The end year
 * can not be determined until the era of the century is known. Once the 
 * era is known, resolveDate() may be called to calculate the year.
 */
public class DeferredHalfCenturyEndDate extends DeferredHalfCenturyDate {

	public DeferredHalfCenturyEndDate(int century, int half) {
		super(century, half);
	}

	@Override
	public void resolveDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		Date startDate = DateUtils.getHalfCenturyEndDate(century, half, era);
		
		setYear(startDate.getYear());
		setMonth(startDate.getMonth());
		setDay(startDate.getDay());
		setEra(startDate.getEra());
	}
}
