package org.collectionspace.services.structureddate.antlr;

import org.collectionspace.services.structureddate.Date;
import org.collectionspace.services.structureddate.DateUtils;
import org.collectionspace.services.structureddate.Era;

public class DeferredQuarterCenturyStartDate extends DeferredQuarterCenturyDate {

	public DeferredQuarterCenturyStartDate(Integer century, Integer quarter) {
		super(century, quarter);
	}

	@Override
	public void finalizeDate() {
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
