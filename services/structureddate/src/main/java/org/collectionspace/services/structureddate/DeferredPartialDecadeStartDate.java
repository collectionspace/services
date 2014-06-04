package org.collectionspace.services.structureddate;

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
