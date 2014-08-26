package org.collectionspace.services.structureddate;

/**
 * A deferred date that represents the start of a millennium. The start year
 * can not be determined until the era of the millennium is known. Once the 
 * era is known, resolveDate() may be called to calculate the year.
 */
public class DeferredMillenniumStartDate extends DeferredMillenniumDate {

	public DeferredMillenniumStartDate(int millennium) {
		super(millennium);
	}

	@Override
	public void resolveDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		Date startDate = DateUtils.getMillenniumStartDate(millennium, era);
		
		setYear(startDate.getYear());
		setMonth(startDate.getMonth());
		setDay(startDate.getDay());
		setEra(startDate.getEra());
	}
}
