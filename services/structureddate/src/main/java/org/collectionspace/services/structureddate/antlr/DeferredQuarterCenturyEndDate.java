package org.collectionspace.services.structureddate.antlr;

import org.collectionspace.services.structureddate.Date;
import org.collectionspace.services.structureddate.DateUtils;
import org.collectionspace.services.structureddate.Era;

public class DeferredQuarterCenturyEndDate extends DeferredQuarterCenturyDate {

	public DeferredQuarterCenturyEndDate(int century, int quarter) {
		super(century, quarter);
	}
	
	@Override
	public void finalizeDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		Date endDate = DateUtils.getQuarterCenturyEndDate(century, quarter, era);
		
		setYear(endDate.getYear());
		setMonth(endDate.getMonth());
		setDay(endDate.getDay());
		setEra(endDate.getEra());
	}
}
