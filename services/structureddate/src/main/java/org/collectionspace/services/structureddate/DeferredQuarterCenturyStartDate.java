package org.collectionspace.services.structureddate;


public class DeferredQuarterCenturyStartDate extends DeferredQuarterCenturyDate {

	public DeferredQuarterCenturyStartDate(int century, int quarter) {
		super(century, quarter);
	}

	@Override
	public void resolveDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		Date startDate = DateUtils.getQuarterCenturyStartDate(century, quarter, era);
		
		setYear(startDate.getYear());
		setMonth(startDate.getMonth());
		setDay(startDate.getDay());
		setEra(startDate.getEra());
	}
}
