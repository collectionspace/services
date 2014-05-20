package org.collectionspace.services.structureddate.antlr;

import org.collectionspace.services.structureddate.Date;
import org.collectionspace.services.structureddate.DateUtils;
import org.collectionspace.services.structureddate.Era;

public class DeferredHalfCenturyEndDate extends DeferredHalfCenturyDate {

	public DeferredHalfCenturyEndDate(int century, int half) {
		super(century, half);
	}

	@Override
	public void finalizeDate() {
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
