package org.collectionspace.services.structureddate;


/**
 * A deferred date that represents the start of a decade. The start year
 * can not be determined until the era of the decade is known. Once the 
 * era is known, resolveDate() may be called to calculate the year.
 */
public class DeferredDecadeStartDate extends DeferredDecadeDate {

	public DeferredDecadeStartDate(int decade) {
		super(decade);
	}
	
	@Override
	public void resolveDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		Date startDate = DateUtils.getDecadeStartDate(decade, era);
		
		setYear(startDate.getYear());
		setMonth(startDate.getMonth());
		setDay(startDate.getDay());
		setEra(startDate.getEra());
	}
}
