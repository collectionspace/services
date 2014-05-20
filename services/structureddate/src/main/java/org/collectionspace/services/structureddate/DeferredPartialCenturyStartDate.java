package org.collectionspace.services.structureddate;

public class DeferredPartialCenturyStartDate extends DeferredPartialCenturyDate {

	public DeferredPartialCenturyStartDate(int century, Part part) {
		super(century, part);
	}

	@Override
	public void resolveDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		Date startDate = DateUtils.getPartialCenturyStartDate(century, part, era);
		
		setYear(startDate.getYear());
		setMonth(startDate.getMonth());
		setDay(startDate.getDay());
		setEra(startDate.getEra());
	}
}
