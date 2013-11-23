package org.collectionspace.services.structureddate;

import org.joda.time.DateTime;
import org.joda.time.chrono.GJChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateUtils {
	private static DateTimeFormatter monthFormatter = DateTimeFormat.forPattern("MMMM");
	
	public static int getMonthByName(String monthName) {
		return monthFormatter.parseDateTime(monthName).getMonthOfYear();
	}
	
	public static int getDaysInMonth(int month, int year) {
		DateTime dateTime = new DateTime(GJChronology.getInstance()).withYear(year).withMonthOfYear(month);
		
		return dateTime.dayOfMonth().getMaximumValue();
	}
	
	/**
	 * Calculates the interval, in years, that should be padded around a date so
	 * that any date within that interval may be considered to be "circa" the
	 * given date. 
	 * 
	 * @param  year The year of the date
	 * @param  era  The era of the date
	 * @return      The number of "circa" years before and after the date  
	 */
	public static int getCircaIntervalYears(int year, Era era) {
		/*
		 * This algorithm is adapted from the fuzzydate parser
		 * in the XDB CineFiles system. Its comment states:
		 * 
		 *   We define circa year/century specifications offsets
 		 *   as +/- 5% of the difference between that year/century
		 *   and the present (2100), so that the farther we go back
		 *   in time, the wider the range of meaning of "circa."
		 * 
		 * Using a fixed year of 2100 to mean "the present" doesn't
		 * make sense to me, so I'm changing "the present" to mean
		 * the actual current year. At the time the XDB code was 
		 * written, using 2100 to mean "the present" resulted in 
		 * current dates having a circa interval of about five years.
		 * To maintain this behavior, I'll add 5 to the result, so 
		 * that the minimum interval is 5 years.
		 */
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		if (era == Era.BCE) {
			year = -year;
		}
		
		int currentYear = new DateTime().year().get();
	
		return (((int) Math.round(Math.abs(currentYear - year) * 0.05)) + 5);
	}
}
