package org.collectionspace.services.structureddate;


/**
 * A deferred date that represents the end of a decade. The end year
 * can not be determined until the era of the decade is known. Once the 
 * era is known, resolveDate() may be called to calculate the year.
 */
public class DeferredDecadeEndDate extends DeferredDecadeDate {

	public DeferredDecadeEndDate(int decade) {
		super(decade);
	}
	
	@Override
	public void resolveDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		Date endDate = DateUtils.getDecadeEndDate(decade, era);
		
		setYear(endDate.getYear());
		setMonth(endDate.getMonth());
		setDay(endDate.getDay());
		setEra(endDate.getEra());
	}
}
