package org.collectionspace.services.structureddate;

public class DeferredPartialDecadeEndDate extends DeferredPartialDecadeDate {

	public DeferredPartialDecadeEndDate(int decade, Part part) {
		super(decade, part);
	}

	@Override
	public void resolveDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		Date startDate = DateUtils.getPartialDecadeEndDate(decade, part, era);
		
		setYear(startDate.getYear());
		setMonth(startDate.getMonth());
		setDay(startDate.getDay());
		setEra(startDate.getEra());
	}
}
