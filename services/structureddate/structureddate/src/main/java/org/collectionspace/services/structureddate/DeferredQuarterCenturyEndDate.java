package org.collectionspace.services.structureddate;


/**
 * A deferred date that represents the end of a quarter century. The end year
 * can not be determined until the era of the century is known. Once the 
 * era is known, resolveDate() may be called to calculate the year.
 */
public class DeferredQuarterCenturyEndDate extends DeferredQuarterCenturyDate {

	public DeferredQuarterCenturyEndDate(int century, int quarter) {
		super(century, quarter);
	}
	
	@Override
	public void resolveDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		Date endDate = DateUtils.getQuarterCenturyEndDate(century, quarter, era);
		
		setYear(endDate.getYear());
		setMonth(endDate.getMonth());
		setDay(endDate.getDay());
		setEra(endDate.getEra());
	}
}
