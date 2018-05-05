package org.collectionspace.services.structureddate;


/**
 * A deferred date that represents the end of a partial century. The end year
 * can not be determined until the era of the century is known. Once the 
 * era is known, resolveDate() may be called to calculate the year.
 */
public class DeferredPartialCenturyEndDate extends DeferredPartialCenturyDate {

	public DeferredPartialCenturyEndDate(int century, Part part) {
		super(century, part);
	}

	@Override
	public void resolveDate() {
		Era era = getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		Date startDate = DateUtils.getPartialCenturyEndDate(century, part, era);
		
		setYear(startDate.getYear());
		setMonth(startDate.getMonth());
		setDay(startDate.getDay());
		setEra(startDate.getEra());
	}
}
