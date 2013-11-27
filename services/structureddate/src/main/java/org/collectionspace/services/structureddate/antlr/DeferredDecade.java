package org.collectionspace.services.structureddate.antlr;

import org.collectionspace.services.structureddate.Date;
import org.collectionspace.services.structureddate.DateUtils;
import org.collectionspace.services.structureddate.Era;

public class DeferredDecade extends Date {
	private Integer decade;
	
	public DeferredDecade(Integer decade) {
		this.decade = decade;
	}
	
	@Override
	public void setEra(Era era) {
		super.setEra(era);

		int startYear = DateUtils.getDecadeStartYear(decade, era);
		
		setYear(startYear);
		setMonth(1);
		setDay(1);
	}
}
