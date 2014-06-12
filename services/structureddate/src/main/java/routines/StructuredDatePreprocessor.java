package routines;

import org.apache.commons.lang.StringUtils;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Talend Open Studio user routines to process date strings into
 * formats that can be unambiguously parsed by the structured
 * date parser.
 */
public class StructuredDatePreprocessor {
	
	/**
	 * Formats Date Acquired strings so they can be 
	 * unambiguously parsed by the structured date parser.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date Preprocessor
	 *
	 * {param} string("display date") input: The display date to format.
	 *
	 * {example} formatDateAcquired("1965-08-06") # 08/06/1965.
	 */
	public static String formatDateAcquired(String date) {
		String formattedDate = date;
		Formatter formatter = new Formatter(date);
		
		boolean success =
			formatter.formatYearMonthDayAsMonthDayYear() ||
			formatter.formatYearNumericMonthAsMonthYear() ||
			formatter.formatMonthDayTwoDigitYearAsMonthDayYear();
		
		if (success) {
			formattedDate = formatter.date;
		}
		
		return formattedDate;
	}
	
	private static class Formatter {
		private static final String[] MONTH_NAMES = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
		
		private static final Pattern YEAR_MONTH_DAY_PATTERN = Pattern.compile("^(\\d{1,4})-(\\d{1,2})-(\\d{1,2})$");
		private static final Pattern YEAR_NUMERIC_MONTH_PATTERN = Pattern.compile("^(\\d{1,4})-(\\d{1,2})$");
		private static final Pattern MONTH_DAY_TWO_DIGIT_YEAR_PATTERN = Pattern.compile("^(\\d{1,2})/(\\d{1,2})/(\\d{2})$");

		private static final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		private static final int currentCentury = (int) Math.floor(currentYear / 100) * 100;
		private static final int previousCentury = currentCentury - 100;
		private static final int currentTwoDigitYear = currentYear - currentCentury;

		public String date;
		
		public Formatter(String date) {
			this.date = StringUtils.trim(date);
		}
		
		public boolean formatYearMonthDayAsMonthDayYear() {
			boolean success = false;
			Matcher matcher = YEAR_MONTH_DAY_PATTERN.matcher(date);
			
			if (matcher.matches()) {
				String year = matcher.group(1);
				String month = matcher.group(2);
				String day = matcher.group(3);
				
				date = month + "/" + day + "/" + year;
				
				success = true;
			}
			
			return success;
		}
		
		public boolean formatYearNumericMonthAsMonthYear() {
			boolean success = false;
			Matcher matcher = YEAR_NUMERIC_MONTH_PATTERN.matcher(date);
			
			if (matcher.matches()) {
				String year = matcher.group(1);
				String month = matcher.group(2);

				int monthIndex = Integer.parseInt(month) - 1;
				
				if (monthIndex >= 0 && monthIndex < 12) {
					String monthName = MONTH_NAMES[monthIndex];
					
					date =  monthName + " " + year;

					success = true;
				}
			}
			
			return success;
		}
		
		public boolean formatMonthDayTwoDigitYearAsMonthDayYear() {
			boolean success = false;
			Matcher matcher = MONTH_DAY_TWO_DIGIT_YEAR_PATTERN.matcher(date);
			
			if (matcher.matches()) {
				String month = matcher.group(1);
				String day = matcher.group(2);
				String year = matcher.group(3);

				int yearNum = Integer.parseInt(year);
				int fullYear = getFullYear(yearNum);
				
				date =  month + "/" + day + "/" + fullYear;	

				success = true;
			}
			
			return success;
		}
		
		private int getFullYear(int year) {
			int fullYear = year;
			
			if (year < 100) {
				if (year <= currentTwoDigitYear) {
					fullYear = currentCentury + year;
				}
				else {
					fullYear = previousCentury + year;
				}
			}
			
			return fullYear;
		}
	}
	
	public static void main(String[] args) {
		// Tests
		
		String dates[] = new String[]{
				"1984-06-12",
				"2007-08",
				"1999-00",
				"1999-01",
				"1999-12",
				"1999-13",
				"12/13/89",
				"2/1/14",
				"3/4/15",
				"6/9/2000",
				"1998-9-22 "
		};
		
		for (String date : dates) {
			System.out.println(date + ": " + formatDateAcquired(date));
		}
	}
}
