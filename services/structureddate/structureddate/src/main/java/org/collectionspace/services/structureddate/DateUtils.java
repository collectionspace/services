package org.collectionspace.services.structureddate;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.MutableDateTime;
import org.joda.time.Years;
import org.joda.time.chrono.GJChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.LocalDate;

public class DateUtils {
	private static final DateTimeFormatter monthFormatter = DateTimeFormat.forPattern("MMMM");
	private static final DateTimeFormatter scalarDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

	// The chronology to use for date calculations, which are done using the joda-time library.
	// See http://www.joda.org/joda-time/apidocs/org/joda/time/Chronology.html for descriptions of
	// the chronologies supported by joda-time.
	
	// GJChronology (http://www.joda.org/joda-time/apidocs/org/joda/time/chrono/GJChronology.html)
	// seems best for representing a mix of modern and historical dates, as might be seen by an
	// anthropology museum.
	
	private static final Chronology chronology = GJChronology.getInstance();
	
	// Define the DateTime that serves as the basis for circa calculations, using the algorithm
	// ported from the XDB date parser. Its comment states:
	//
	//    We define circa year/century specifications offsets
	//    as +/- 5% of the difference between that year/century
	//    and the present (2100), so that the farther we go back
	//    in time, the wider the range of meaning of "circa."
	
	private static final DateTime circaBaseDateTime = new DateTime(2100, 12, 31, 0, 0, 0, 0, chronology);
	
	/**
	 * Gets the number (1-12) of a month for a given name.
	 * 
	 * @param monthName The name of the month
	 * @return          The number of the month, between 1 and 12
	 */
	public static int getMonthByName(String monthName) {
		// Normalize "sept" to "sep", since DateTimeFormat doesn't
		// understand the former.
		
		if (monthName.equals("sept")) {
			monthName = "sep";
		}
		
		return monthFormatter.parseDateTime(monthName).getMonthOfYear();
	}
	
	/**
	 * Gets the number of days in a given month.
	 * 
	 * @param month The month number, between 1 and 12
	 * @param year  The year (in order to account for leap years)
	 * @return      The number of days in the month
	 */
	public static int getDaysInMonth(int month, int year, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		DateTime dateTime = new DateTime(chronology)
				.withEra((era == Era.BCE) ? DateTimeConstants.BC : DateTimeConstants.AD)
				.withYearOfEra(year)
				.withMonthOfYear(month);

		return dateTime.dayOfMonth().getMaximumValue();
	}
	
	/**
	 * Gets the Date representing the first day of a given quarter year.
	 * 
	 * @param year    The year
	 * @param quarter The quarter, between 1 and 4
	 * @return        The first day of the quarter year
	 */
	public static Date getQuarterYearStartDate(int quarter, int year) {
		int startMonth = getQuarterYearStartMonth(quarter);
		
		return new Date(year, startMonth, 1);
	}
	
	/**
	 * Gets the Date representing the last day of a given quarter year.
	 * 
	 * @param year    The year
	 * @param quarter The quarter, between 1 and 4
	 * @return        The last day of the quarter year
	 */
	public static Date getQuarterYearEndDate(int quarter, int year, Era era) {
		int endMonth = getQuarterYearEndMonth(quarter);
		
		return new Date(year, endMonth, DateUtils.getDaysInMonth(endMonth, year, era));
	}
	
	/**
	 * Gets the first month of a given quarter in a year.
	 * 
	 * @param quarter The quarter, between 1 and 4
	 * @return        The number of the first month in the quarter
	 */
	public static int getQuarterYearStartMonth(int quarter) {
		return ((3 * (quarter-1)) + 1);
	}
	
	/**
	 * Gets the last month of a given quarter in a year.
	 * 
	 * @param quarter The quarter, between 1 and 4
	 * @return        The number of the last month in the quarter
	 */
	public static int getQuarterYearEndMonth(int quarter) {
		return (getQuarterYearStartMonth(quarter) + 2);
	}

	/**
	 * Gets the Date representing the first day of a given half year.
	 * 
	 * @param year The year
	 * @param half The half, between 1 and 2
	 * @return     The first day of the half year
	 */
	public static Date getHalfYearStartDate(int half, int year) {
		int startMonth = getHalfYearStartMonth(half);
		
		return new Date(year, startMonth, 1);
	}


	/**
	 * Gets the Date representing the last day of a given half year.
	 * 
	 * @param year The year
	 * @param half The half, between 1 and 2
	 * @return     The last day of the half year
	 */
	public static Date getHalfYearEndDate(int half, int year, Era era) {
		int endMonth = getHalfYearEndMonth(half);
		
		return new Date(year, endMonth, DateUtils.getDaysInMonth(endMonth, year, era));
	}

	/**
	 * Gets the first month of a given half in a year.
	 * 
	 * @param half The half, between 1 and 2
	 * @return     The number of the first month in the half
	 */
	public static int getHalfYearStartMonth(int half) {
		return ((6 * (half-1)) + 1);
	}

	/**
	 * Gets the last month of a given half in a year.
	 * 
	 * @param half The half, between 1 and 2
	 * @return     The number of the last month in the half
	 */
	public static int getHalfYearEndMonth(int half) {
		return (getHalfYearStartMonth(half) + 5);
	}
	
	/**
	 * Gets the Date representing the first day of a given partial year.
	 * 
	 * @param year The year
	 * @param part The part
	 * @return     The first day of the partial year
	 */
	public static Date getPartialYearStartDate(Part part, int year) {
		int startMonth = getPartialYearStartMonth(part);
		
		return new Date(year, startMonth, 1);
	}

	/**
	 * Gets the Date representing the last day of a given partial year.
	 * 
	 * @param year The year
	 * @param part The part
	 * @return     The last day of the partial year
	 */
	public static Date getPartialYearEndDate(Part part, int year, Era era) {
		int endMonth = getPartialYearEndMonth(part);
		
		return new Date(year, endMonth, DateUtils.getDaysInMonth(endMonth, year, era));
	}
	
	/**
	 * Gets the first month of a given part of a year.
	 * 
	 * @param part The part
	 * @return     The number of the first month in the part
	 */
	public static int getPartialYearStartMonth(Part part) {
		int month;
		
		if (part == Part.EARLY) {
			month = 1;
		}
		else if (part == Part.MIDDLE) {
			month = 5;
		}
		else if (part == Part.LATE) {
			month = 9;
		}
		else {
			throw new IllegalArgumentException("unexpected part");
		}
		
		return month;
	}
	
	/**
	 * Gets the last month of a given part of a year.
	 * 
	 * @param part The part
	 * @return     The number of the last month in the part
	 */
	public static int getPartialYearEndMonth(Part part) {
		int month;
		
		if (part == Part.EARLY) {
			month = 4;
		}
		else if (part == Part.MIDDLE) {
			month = 8;
		}
		else if (part == Part.LATE) {
			month = 12;
		}
		else {
			throw new IllegalArgumentException("unexpected part");
		}
		
		return month;
	}
	
	/**
	 * Gets the Date representing the first day of a given partial decade.
	 * 
	 * @param decade The decade, specified as a number ending in 0.
	 *               For decades A.D., this is the first year of the decade. For 
	 *               decades B.C., this is the last year of the decade.
	 * @param part   The part
	 * @param era    The era of the decade. If null, Date.DEFAULT_ERA is assumed.
	 * @return       The first day of the partial decade
	 */
	public static Date getPartialDecadeStartDate(int decade, Part part, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		int startYear = getPartialDecadeStartYear(decade, part, era);
		
		return new Date(startYear, 1, 1, era);
	}

	/**
	 * Gets the Date representing the last day of a given partial decade.
	 * 
	 * @param decade The decade, specified as a number ending in 0.
	 *               For decades A.D., this is the first year of the decade. For 
	 *               decades B.C., this is the last year of the decade.
	 * @param part   The part
	 * @param era    The era of the decade. If null, Date.DEFAULT_ERA is assumed.
	 * @return       The last day of the partial decade
	 */
	public static Date getPartialDecadeEndDate(int decade, Part part, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		int endYear = getPartialDecadeEndYear(decade, part, era);
		
		return new Date(endYear, 12, 31, era);
	}
	
	/**
	 * Gets the first year of a given part of a decade.
	 * 
	 * @param decade The decade, specified as a number ending in 0.
	 *               For decades A.D., this is the first year of the decade. For 
	 *               decades B.C., this is the last year of the decade.
	 * @param part   The part
	 * @param era    The era of the decade. If null, Date.DEFAULT_ERA is assumed.
	 * @return       The first year in the part
	 */
	public static int getPartialDecadeStartYear(int decade, Part part, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int startYear;
		
		if (era == Era.BCE) {
			if (part == Part.EARLY) {
				startYear = decade + 9;
			}
			else if (part == Part.MIDDLE) {
				startYear = decade + 6;
			}
			else if (part == Part.LATE) {
				startYear = decade + 3;
			}
			else {
				throw new IllegalArgumentException("unexpected part");
			}
		}
		else {
			if (part == Part.EARLY) {
				startYear = decade;
			}
			else if (part == Part.MIDDLE) {
				startYear = decade + 4;
			}
			else if (part == Part.LATE) {
				startYear = decade + 7;
			}
			else {
				throw new IllegalArgumentException("unexpected part");
			}
		}
		
		return startYear;
	}
	
	/**
	 * Gets the last year of a given part of a decade.
	 * 
	 * @param decade The decade, specified as a number ending in 0.
	 *               For decades A.D., this is the first year of the decade. For 
	 *               decades B.C., this is the last year of the decade.
	 * @param part   The part
	 * @param era    The era of the decade. If null, Date.DEFAULT_ERA is assumed.
	 * @return       The last year in the part
	 */
	public static int getPartialDecadeEndYear(int decade, Part part, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int endYear;
		
		if (era == Era.BCE) {
			if (part == Part.EARLY) {
				endYear = decade + 7;
			}
			else if (part == Part.MIDDLE) {
				endYear = decade + 4;
			}
			else if (part == Part.LATE) {
				endYear = decade;
			}
			else {
				throw new IllegalArgumentException("unexpected part");
			}
		}
		else {
			if (part == Part.EARLY) {
				endYear = decade + 3;
			}
			else if (part == Part.MIDDLE) {
				endYear = decade + 6;
			}
			else if (part == Part.LATE) {
				endYear = decade + 9;
			}
			else {
				throw new IllegalArgumentException("unexpected part");
			}
		}
		
		return endYear;
	}

	/**
	 * Gets the Date representing the first day of a given decade.
	 * 
	 * @param decade The decade, specified as a number ending in 0.
	 *               For decades A.D., this is the first year of the decade. For 
	 *               decades B.C., this is the last year of the decade.
	 * @param era    The era of the decade. If null, Date.DEFAULT_ERA is assumed.
	 * @return       The first day of the decade
	 */
	public static Date getDecadeStartDate(int decade, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		int startYear = getDecadeStartYear(decade, era);
		
		return new Date(startYear, 1, 1, era);
	}
	
	/**
	 * Gets the Date representing the last day of a given decade.
	 * 
	 * @param decade The decade, specified as a number ending in 0.
	 *               For decades A.D., this is the first year of the decade. For 
	 *               decades B.C., this is the last year of the decade.
	 * @param era    The era of the decade. If null, Date.DEFAULT_ERA is assumed.
	 * @return       The last day of the decade
	 */
	public static Date getDecadeEndDate(int decade, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		int endYear = getDecadeEndYear(decade, era);
		
		return new Date(endYear, 12, 31, era);
	}
	
	/**
	 * Gets the first year of a given decade.
	 * 
	 * @param decade The decade, specified as a number ending in 0.
	 *               For decades A.D., this is the first year of the decade. For 
	 *               decades B.C., this is the last year of the decade.
	 * @param era    The era of the decade. If null, Date.DEFAULT_ERA is assumed.
	 * @return       The first year of the decade
	 */
	public static int getDecadeStartYear(int decade, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int startYear;
		
		if (era == Era.BCE) {
			startYear = decade + 9;
		}
		else {
			startYear = decade;
		}
		
		return startYear;
	}

	/**
	 * Gets the last year of a given decade.
	 * 
	 * @param decade The decade, specified as a number ending in 0.
	 *               For decades A.D., this is the first year of the decade. For 
	 *               decades B.C., this is the last year of the decade.
	 * @param era    The era of the decade. If null, Date.DEFAULT_ERA is assumed.
	 * @return       The last year of the decade
	 */
	public static int getDecadeEndYear(int decade, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int endYear;
		
		if (era == Era.BCE) {
			endYear = decade;
		}
		else {
			endYear = decade + 9;
		}
		
		return endYear;
	}
		
	/**
	 * Gets the Date representing the first day of a given century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The first day of the century
	 */
	public static Date getCenturyStartDate(int century, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		int startYear = getCenturyStartYear(century, era);
		
		return new Date(startYear, 1, 1, era);
	}

	/**
	 * Gets the Date representing the last day of a given century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The last day of the century
	 */
	public static Date getCenturyEndDate(int century, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		int endYear = getCenturyEndYear(century, era);
		
		return new Date(endYear, 12, 31, era);
	}
	
	/**
	 * Gets the first year of a given century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The first year of the century
	 */
	public static int getCenturyStartYear(int century, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int startYear;
		
		if (era == Era.BCE) {
			startYear = century + 99;
		}
		else {
			startYear = century;
		}
		
		return startYear;
	}
	
	/**
	 * Gets the last year of a given century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The last year of the century
	 */
	public static int getCenturyEndYear(int century, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int endYear;
		
		if (era == Era.BCE) {
			endYear = century;
		}
		else {
			endYear = century + 99;
		}
		
		return endYear;
	}
	
	/**
	 * Gets the Date representing the first day of a given partial century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param part    The part
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The first day of the partial century
	 */
	public static Date getPartialCenturyStartDate(int century, Part part, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		int startYear = getPartialCenturyStartYear(century, part, era);
		
		return new Date(startYear, 1, 1, era);
	}
	
	/**
	 * Gets the Date representing the last day of a given partial century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param part    The part
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The last day of the partial century
	 */
	public static Date getPartialCenturyEndDate(int century, Part part, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		int endYear = getPartialCenturyEndYear(century, part, era);
		
		return new Date(endYear, 12, 31, era);
	}
	
	/**
	 * Gets the first year of a given partial century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param part    The part
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The first year of the partial century
	 */
	public static int getPartialCenturyStartYear(int century, Part part, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int startYear;
		
		if (era == Era.BCE) {
			if (part == Part.EARLY) {
				startYear = century + 99;
			}
			else if (part == Part.MIDDLE) {
				startYear = century + 66;
			}
			else if (part == Part.LATE) {
				startYear = century + 33;
			}
			else {
				throw new IllegalArgumentException("unexpected part");
			}
		}
		else {
			if (part == Part.EARLY) {
				startYear = century;
			}
			else if (part == Part.MIDDLE) {
				startYear = century + 33;
			}
			else if (part == Part.LATE) {
				startYear = century + 66;
			}
			else {
				throw new IllegalArgumentException("unexpected part");
			}
		}
		
		return startYear;
	}
	
	/**
	 * Gets the last year of a given partial century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param part    The part
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The last year of the partial century
	 */
	public static int getPartialCenturyEndYear(int century, Part part, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int endYear;
		
		if (era == Era.BCE) {
			if (part == Part.EARLY) {
				endYear = century + 66;
			}
			else if (part == Part.MIDDLE) {
				endYear = century + 33;
			}
			else if (part == Part.LATE) {
				endYear = century;
			}
			else {
				throw new IllegalArgumentException("unexpected part");
			}
		}
		else {
			if (part == Part.EARLY) {
				endYear = century + 33;
			}
			else if (part == Part.MIDDLE) {
				endYear = century + 66;
			}
			else if (part == Part.LATE) {
				endYear = century + 99;
			}
			else {
				throw new IllegalArgumentException("unexpected part");
			}
		}
		
		return endYear;
	}
	
	/**
	 * Gets the Date representing the first day of a given half century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param half    The half
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The first day of the half century
	 */
	public static Date getHalfCenturyStartDate(int century, int half, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		int startYear = getHalfCenturyStartYear(century, half, era);
		
		return new Date(startYear, 1, 1, era);
	}

	/**
	 * Gets the Date representing the last day of a given half century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param half    The half
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The last day of the half century
	 */
	public static Date getHalfCenturyEndDate(int century, int half, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		int endYear = getHalfCenturyEndYear(century, half, era);
		
		return new Date(endYear, 12, 31, era);
	}
	
	/**
	 * Gets the first year of a given half century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param half    The half
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The first year of the half century
	 */
	public static int getHalfCenturyStartYear(int century, int half, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int startYear;
		
		if (era == Era.BCE) {
			startYear = (century + 99) - (50 * (half - 1));
		}
		else {
			startYear = century + (50 * (half - 1));
		}
		
		return startYear;
	}
	
	/**
	 * Gets the last year of a given half century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param half    The half
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The last year of the half century
	 */
	public static int getHalfCenturyEndYear(int century, int half, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int endYear;
		
		if (era == Era.BCE) {
			endYear = (century + 99) - (50 * half) + 1;
		}
		else {
			endYear = century + (50 * half) - 1;
		}
		
		return endYear;
	}
	
	/**
	 * Gets the Date representing the first day of a given quarter century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param quarter The quarter
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The first day of the quarter century
	 */
	public static Date getQuarterCenturyStartDate(int century, int quarter, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int startYear = getQuarterCenturyStartYear(century, quarter, era);
		
		return new Date(startYear, 1, 1, era);
	}

	/**
	 * Gets the Date representing the last day of a given quarter century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param quarter The quarter
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The last day of the quarter century
	 */
	public static Date getQuarterCenturyEndDate(int century, int quarter, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int endYear = getQuarterCenturyEndYear(century, quarter, era);
		
		return new Date(endYear, 12, 31, era);
	}
	
	/**
	 * Gets the first year of a given quarter century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param quarter The quarter
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The first year of the quarter century
	 */
	public static int getQuarterCenturyStartYear(int century, int quarter, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int startYear;
		
		if (era == Era.BCE) {
			startYear = (century + 99) - (25 * (quarter - 1));
		}
		else {
			startYear = century + (25 * (quarter - 1));
		}
		
		return startYear;
	}
	
	/**
	 * Gets the last year of a given quarter century.
	 * 
	 * @param century The century, specified as a number ending in 00 or 01.
	 *                For centuries A.D., this is the first year of the century. For 
	 *                centuries B.C., this is the last year of the century. For example,
	 *                the "21st century" would be specified as 2001, whereas the "2000's"
	 *                would be specified as 2000. The "2nd century B.C." would be specified
	 *                as 101. The "100's B.C." would be specified as 100.
	 * @param quarter The quarter
	 * @param era     The era of the century. If null, Date.DEFAULT_ERA is assumed.
	 * @return        The last year of the quarter century
	 */
	public static int getQuarterCenturyEndYear(int century, int quarter, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int endYear;
		
		if (era == Era.BCE) {
			endYear = (century + 99) - (25 * quarter) + 1;
		}
		else {
			endYear = century + (25 * quarter) - 1;
		}
		
		return endYear;
	}
	
	/**
	 * Converts an nth century number to a year. For example, to convert "21st century"
	 * to a year, call nthCenturyToYear(21), which returns 2001. For centuries A.D., the
	 * year returned is the first year of the nth century. For centuries B.C., the
	 * year returned is the last year of the nth century. 
	 * 
	 * @param n The nth century number
	 * @return  The first year in the nth century, for centuries A.D.
	 *          The last year of the nth century, for centuries B.C.
	 */
	public static int nthCenturyToYear(int n) {
		int year = (n-1) * 100 + 1;
		
		return year;
	}
	
	/**
	 * Gets the Date representing the first day of a given millennium.
	 * 
	 * @param n   The nth millennium number
	 * @param era The era of the millennium. If null, Date.DEFAULT_ERA is assumed.
	 * @return    The first day of the millennium
	 */
	public static Date getMillenniumStartDate(int n, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		int startYear = getMillenniumStartYear(n, era);
		
		return new Date(startYear, 1, 1, era);
	}

	/**
	 * Gets the Date representing the last day of a given millennium.
	 * 
	 * @param n   The nth millennium number
	 * @param era The era of the millennium. If null, Date.DEFAULT_ERA is assumed.
	 * @return    The last day of the millennium
	 */
	public static Date getMillenniumEndDate(int n, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		int endYear = getMillenniumEndYear(n, era);
		
		return new Date(endYear, 12, 31, era);
	}
	
	/**
	 * Gets the first year of a given millennium.
	 * 
	 * @param n   The nth millennium number
	 * @param era The era of the millennium. If null, Date.DEFAULT_ERA is assumed.
	 * @return    The first year of the millennium
	 */
	public static int getMillenniumStartYear(int n, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int year;

		if (era == Era.BCE) {
			year = n * 1000;
		}
		else {
			year = (n - 1) * 1000 + 1;
		}
		
		return year;
	}

	/**
	 * Gets the last year of a given millennium.
	 * 
	 * @param n   The nth millennium number
	 * @param era The era of the millennium. If null, Date.DEFAULT_ERA is assumed.
	 * @return    The last year of the millennium
	 */
	public static int getMillenniumEndYear(int n, Era era) {
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		int year;

		if (era == Era.BCE) {
			year = (n - 1) * 1000 + 1;
		}
		else {
			year = n * 1000;
		}
		
		return year;
	}
	
	/**
	 * Calculates the earliest date that may be considered to be "before"
	 * a given date.
	 * 
	 * @param date The date
	 * @return     The earliest date "before" the date
	 */
	public static Date getEarliestBeforeDate(Date date) {
		return getEarliestBeforeDate(date, null);
	}
	
	/**
	 * Calculates the latest date that may be considered to be "after"
	 * a given date.
	 * 
	 * @param date The date
	 * @return     The latest date "after" the date
	 */
	public static Date getLatestAfterDate(Date date) {
		return getLatestAfterDate(date, null);
	}
	
	/**
	 * Calculates the earliest date that may be considered to be "before"
	 * a given date range.
	 * 
	 * @param startDate The first date in the range
	 * @param endDate   The last date in the range
	 * @return          The earliest date "before" the range
	 */
	public static Date getEarliestBeforeDate(Date startDate, Date endDate) {
		// TODO
		// Return an empty date to be used in before date cases
		return new Date();
		
		/*
		// This algorithm is inherited from the XDB fuzzydate parser,
		// which considers "before" to mean "within a lifetime before".

		if (endDate == null) {
			endDate = startDate;
		}
		
		int difference = getYearsBetween(startDate, endDate);
		
		Date earliestDate = startDate.copy();
		subtractYears(earliestDate, 1);
		earliestDate.setMonth(1);
		earliestDate.setDay(1);
		
		if (difference < 100) {
			// The comment from the XDB fuzzydate parser states:
			//
			//    Before/after years are really about birth/death dates
			//    so we use average life-span of 75 years

			subtractYears(earliestDate, 75);
		}
		else {
			// The comment from the XDB fuzzydate parser states:
			//
			//    Before/after years are really about birth/death dates
			//    so we use average life-span of 75 years
			//    but since  the spec was a century, e.g. pre 20th century
			//    we'll make the range a bit bigger
			//    sheesh...

			subtractYears(earliestDate, 175);
		}
		
		return earliestDate;
		*/
	}
	
	/**
	 * Calculates the latest date that may be considered to be "after"
	 * a given date range. We define "after" as the current date.
	 * 
	 * @param startDate The first date in the range
	 * @param endDate   The last date in the range
	 * @return          The latest date "after" the range
	 */
	public static Date getLatestAfterDate(Date startDate, Date endDate) {
		// TODO
		LocalDate localDate = new LocalDate();
		Integer year = (Integer) localDate.getYear();
		Integer month = (Integer) localDate.getMonthOfYear();
		Integer dayOfMonth = (Integer) localDate.getDayOfMonth();
		return new Date(year, month, dayOfMonth, Date.DEFAULT_ERA);

	}

	public static int getYearsBetween(Date startDate, Date endDate) {
		if (startDate == null || endDate == null) {
			throw new InvalidDateException("date must not be null");
		}
		
		Integer startYear = startDate.getYear();
		Integer endYear = endDate.getYear();
		
		if (startYear == null || endYear == null) {
			throw new IllegalArgumentException("year must not be null");
		}
		
		Era startEra = startDate.getEra();
		Era endEra = endDate.getEra();
		
		if (startEra == null || endEra == null) {
			throw new IllegalArgumentException("era must not be null");
		}
		
		MutableDateTime startDateTime = convertToDateTime(startDate);
		MutableDateTime endDateTime = convertToDateTime(endDate);
		
		int years = Years.yearsBetween(startDateTime, endDateTime).getYears();
		
		return years;
	}
	
	/**
	 * Calculates the interval, in years, that should be padded around a date so
	 * that any date within that interval may be considered to be "circa" the
	 * given date. 
	 * 
	 * @param  year The year of the date
	 * @param  era  The era of the date. If null, Date.DEFAULT_ERA is assumed.
	 * @return      The number of "circa" years before and after the date
	 */
	public static int getCircaIntervalYears(int year, Era era) {
		/*
		 * This algorithm is inherited from the fuzzydate parser
		 * in XDB. Its comment states:
		 * 
		 *   We define circa year/century specifications offsets
 		 *   as +/- 5% of the difference between that year/century
		 *   and the present (2100), so that the farther we go back
		 *   in time, the wider the range of meaning of "circa."
		 *    
		 */
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		MutableDateTime dateTime = new MutableDateTime(chronology);
		dateTime.era().set((era == Era.BCE) ? DateTimeConstants.BC : DateTimeConstants.AD);
		dateTime.yearOfEra().set(year);
		dateTime.monthOfYear().set(1);
		dateTime.dayOfMonth().set(1);
		dateTime.setTime(0, 0, 0, 0);
		
		int years = Years.yearsBetween(dateTime, circaBaseDateTime).getYears();

		return ((int) Math.round(years * 0.05));
	}
	
	/**
	 * Adds a number of days to a date.
	 * 
	 * @param date The date	
	 * @param days The number of days to add to the date
	 */
	public static void addDays(Date date, int days) {
		MutableDateTime dateTime = convertToDateTime(date);
		
		dateTime.add(Days.days(days));
		
		setFromDateTime(date, dateTime);
	}
	
	/**
	 * Adds a number of years to a date's year.
	 * 
	 * @param date  The date	
	 * @param years The number of years to add to the date
	 */
	public static void addYears(Date date, int years) {
		MutableDateTime dateTime = convertToDateTime(date);

		dateTime.add(Years.years(years));
		
		setFromDateTime(date, dateTime);
	}
	
	/**
	 * Subtracts a number of years from a date's year.
	 * 
	 * @param date  The date	
	 * @param years The number of years to subtract from the date
	 */
	public static void subtractYears(Date date, int years) {
		addYears(date, -years);
	}
	
	public static String getEarliestTimestamp(Date date) {
		Era era = date.getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		MutableDateTime dateTime = null;
		
		try {
			dateTime = convertToDateTime(date);
		}
		catch(IllegalFieldValueException e) {
			throw new InvalidDateException(e.getMessage());
		}
		
		String scalarDate = scalarDateFormatter.print(dateTime);
		
		return scalarDate;
	}
	
	public static String getLatestTimestamp(Date date) {
		Era era = date.getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}
		
		MutableDateTime dateTime = null;
		
		try {
			dateTime = convertToDateTime(date);
		}
		catch(IllegalFieldValueException e) {
			throw new InvalidDateException(e.getMessage());
		}
		
		dateTime.setTime(23, 59, 59, 999);
		
		String scalarDate = scalarDateFormatter.print(dateTime);
		
		return scalarDate;
	}
	
	public static boolean isValidDate(int year, int month, int day, Era era) {
		boolean isValid = true;
		
		try {
			convertToDateTime(new Date(year, month,day, era));
		}
		catch(IllegalFieldValueException e) {
			isValid = false;
		}
		
		return isValid;
	}
	
	/**
	 * Converts a Date to a joda-time DateTime.
	 * 
	 * @param  date The Date
	 * @return      A MutableDateTime representing the same date
	 */
	private static MutableDateTime convertToDateTime(Date date) {
		Era era = date.getEra();
		
		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		MutableDateTime dateTime = new MutableDateTime(chronology);
		dateTime.era().set((era == Era.BCE) ? DateTimeConstants.BC : DateTimeConstants.AD);
		dateTime.yearOfEra().set(date.getYear());
		dateTime.monthOfYear().set(date.getMonth());
		dateTime.dayOfMonth().set(date.getDay());
		dateTime.setTime(0, 0, 0, 0);

		return dateTime;
	}
	
	/**
	 * Sets the fields in a Date so that it represents the same date
	 * as a given DateTime.
	 * 
	 * @param date     The Date to set
	 * @param dateTime A MutableDateTime representing the desired date
	 */
	private static void setFromDateTime(Date date, MutableDateTime dateTime) {
		date.setYear(dateTime.getYearOfEra());
		date.setMonth(dateTime.getMonthOfYear());
		date.setDay(dateTime.getDayOfMonth());
		date.setEra((dateTime.getEra() == DateTimeConstants.BC) ? Era.BCE : Era.CE);
	}
}
