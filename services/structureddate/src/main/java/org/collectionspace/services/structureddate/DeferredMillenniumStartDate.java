package org.collectionspace.services.structureddate;

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
