package org.collectionspace.services.structureddate;


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
