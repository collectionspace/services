package org.collectionspace.services.structureddate;

/**
 * A deferred date that represents the start of a partial century. The start year
 * can not be determined until the era of the century is known. Once the 
 * era is known, resolveDate() may be called to calculate the year.
 */
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
