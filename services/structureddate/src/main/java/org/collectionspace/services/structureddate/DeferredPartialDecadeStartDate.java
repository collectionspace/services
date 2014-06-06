package org.collectionspace.services.structureddate;

/**
 * A deferred date that represents the start of a partial decade. The start year
 * can not be determined until the era of the decade is known. Once the 
 * era is known, resolveDate() may be called to calculate the year.
 */
public class DeferredPartialDecadeStartDate extends DeferredPartialDecadeDate {

	public DeferredPartialDecadeStartDate(int decade, Part part) {
		super(decade, part);
	}

	@Override
	public void resolveDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		Date startDate = DateUtils.getPartialDecadeStartDate(decade, part, era);
		
		setYear(startDate.getYear());
		setMonth(startDate.getMonth());
		setDay(startDate.getDay());
		setEra(startDate.getEra());
	}
}
