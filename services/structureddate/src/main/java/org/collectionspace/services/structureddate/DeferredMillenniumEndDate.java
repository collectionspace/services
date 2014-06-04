package org.collectionspace.services.structureddate;

public class DeferredMillenniumEndDate extends DeferredMillenniumDate {

	public DeferredMillenniumEndDate(int millennium) {
		super(millennium);
	}

	@Override
	public void resolveDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		Date startDate = DateUtils.getMillenniumEndDate(millennium, era);
		
		setYear(startDate.getYear());
		setMonth(startDate.getMonth());
		setDay(startDate.getDay());
		setEra(startDate.getEra());
	}
}
