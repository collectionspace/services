package org.collectionspace.services.structureddate;

/**
 * A deferred date that represents the end of a month. The end day
 * can not be determined until the year and era of the month are known. Once the 
 * year and era are known, resolveDate() may be called to calculate the day.
 */
public class DeferredMonthEndDate extends DeferredDate {

	@Override
	public void resolveDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int day = DateUtils.getDaysInMonth(getMonth(), getYear(), era);
		
		setDay(day);
	}
}
