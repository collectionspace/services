package org.collectionspace.services.structureddate;


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
