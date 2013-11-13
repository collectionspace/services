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
}
