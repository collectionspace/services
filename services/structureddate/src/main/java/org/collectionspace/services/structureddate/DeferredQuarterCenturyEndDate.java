package org.collectionspace.services.structureddate;


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
