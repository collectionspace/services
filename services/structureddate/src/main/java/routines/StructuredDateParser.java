package routines;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.structureddate.Certainty;
import org.collectionspace.services.structureddate.Date;
import org.collectionspace.services.structureddate.Era;
import org.collectionspace.services.structureddate.InvalidDateException;
import org.collectionspace.services.structureddate.QualifierType;
import org.collectionspace.services.structureddate.QualifierUnit;
import org.collectionspace.services.structureddate.StructuredDate;
import org.collectionspace.services.structureddate.StructuredDateFormatException;

/**
 * Talend Open Studio user routines that wrap the structured date parser.
 */
public class StructuredDateParser {
	
	// Cache the last date parsed.
	private static ParseResult cachedParseResult = new ParseResult();

	/**
	 * Tests if a display date string is a valid structured date.
	 * Returns true if the date string is able to be parsed, and passes
	 * range validation checks for all fields. Returns false otherwise.
	 * 
	 * {talendTypes} boolean
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} isValid("June 9, 2014") # true.
	 */
	public static boolean isValid(String displayDate) {
		return parse(displayDate).isValid;
	}
	
	/**
	 * Returns a message describing why a display date string is not
	 * valid. Returns null if the display date is valid.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getErrorMessage("December 231, 1978") # Value 231 for dayOfMonth must be in the range [1,31].
	 */
	public static String getErrorMessage(String displayDate) {
		String value = "";
		String message = parse(displayDate).errorMessage;
		
		if (message != null) {
			value = message;
		}
		
		return value;
	}
	
	/**
	 * Returns the earliest/single year for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getEarliestSingleYear("June 9, 2014") # 2014.
	 */
	public static String getEarliestSingleYear(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getEarliestSingleDate();
			
			if (date != null) {
				Integer year = date.getYear();
				
				if (year != null) {
					value = year.toString();
				}
			}
		}
		
		return value;
	}
	
	/**
	 * Returns the earliest/single month for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getEarliestSingleMonth("June 9, 2014") # 6.
	 */
	public static String getEarliestSingleMonth(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getEarliestSingleDate();
			
			if (date != null) {
				Integer month = date.getMonth();
				
				if (month != null) {
					value = month.toString();
				}
			}
		}
		
		return value;
	}

	/**
	 * Returns the earliest/single day for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getEarliestSingleDay("June 9, 2014") # 9.
	 */
	public static String getEarliestSingleDay(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getEarliestSingleDate();
			
			if (date != null) {
				Integer day = date.getDay();
				
				if (day != null) {
					value = day.toString();
				}
			}
		}
		
		return value;
	}

	/**
	 * Returns the earliest/single era for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getEarliestSingleEra("June 9, 2014") # urn:cspace:bampfa.cspace.berkeley.edu:vocabularies:name(dateera):item:name(ce)'AD'.
	 */
	public static String getEarliestSingleEra(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getEarliestSingleDate();
			
			if (date != null) {
				Era era = date.getEra();
				
				if (era != null) {
					value = era.toString();
				}
			}
		}
		
		return value;
	}
	
	/**
	 * Returns the earliest/single certainty for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getEarliestSingleCertainty("June 9, 2014") # .
	 */
	public static String getEarliestSingleCertainty(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getEarliestSingleDate();
			
			if (date != null) {
				Certainty cirtainty = date.getCertainty();
				
				if (cirtainty != null) {
					value = cirtainty.toString();
				}
			}
		}
		
		return value;
	}
	
	/**
	 * Returns the earliest/single qualifier type for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getEarliestSingleQualifierType("June 9, 2014") # .
	 */
	public static String getEarliestSingleQualifierType(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getEarliestSingleDate();
			
			if (date != null) {
				QualifierType qualifierType = date.getQualifierType();
				
				if (qualifierType != null) {
					value = qualifierType.toString();
				}
			}
		}
		
		return value;
	}
	
	/**
	 * Returns the earliest/single qualifier value for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getEarliestSingleQualifierValue("June 9, 2014") # .
	 */
	public static String getEarliestSingleQualifierValue(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getEarliestSingleDate();
			
			if (date != null) {
				Integer qualifierValue = date.getQualifierValue();
				
				if (qualifierValue != null) {
					value = qualifierValue.toString();
				}
			}
		}
		
		return value;
	}

	/**
	 * Returns the earliest/single qualifier unit for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getEarliestSingleQualifierUnit("June 9, 2014") # .
	 */
	public static String getEarliestSingleQualifierUnit(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getEarliestSingleDate();
			
			if (date != null) {
				QualifierUnit qualifierUnit = date.getQualifierUnit();
				
				if (qualifierUnit != null) {
					value = qualifierUnit.toString();
				}
			}
		}
		
		return value;
	}
	
	/**
	 * Returns the earliest/single scalar date for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getEarliestSingleScalarDate("June 9, 2014") # 2014-06-09T00:00:00Z.
	 */
	public static String getEarliestSingleScalarDate(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			String scalarDate = structuredDate.getEarliestScalarDate();
			
			if (scalarDate != null) {
				value = scalarDate;
			}
		}
		
		return value;
	}
	
	/**
	 * Returns the latest year for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getLatestYear("June 9, 2014") # 2014.
	 */
	public static String getLatestYear(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getLatestDate();
			
			if (date != null) {
				Integer year = date.getYear();
				
				if (year != null) {
					value = year.toString();
				}
			}
		}
		
		return value;
	}
	
	/**
	 * Returns the latest month for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getLatestMonth("June 9, 2014") # 6.
	 */
	public static String getLatestMonth(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getLatestDate();
			
			if (date != null) {
				Integer month = date.getMonth();
				
				if (month != null) {
					value = month.toString();
				}
			}
		}
		
		return value;
	}

	/**
	 * Returns the latest day for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getLatestDay("June 9, 2014") # 9.
	 */
	public static String getLatestDay(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getLatestDate();
			
			if (date != null) {
				Integer day = date.getDay();
				
				if (day != null) {
					value = day.toString();
				}
			}
		}
		
		return value;
	}

	/**
	 * Returns the latest era for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getLatestEra("June 9, 2014") # urn:cspace:bampfa.cspace.berkeley.edu:vocabularies:name(dateera):item:name(ce)'AD'.
	 */
	public static String getLatestEra(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getLatestDate();
			
			if (date != null) {
				Era era = date.getEra();
				
				if (era != null) {
					value = era.toString();
				}
			}
		}
		
		return value;
	}
	
	/**
	 * Returns the latest certainty for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getLatestCertainty("June 9, 2014") # .
	 */
	public static String getLatestCertainty(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getLatestDate();
			
			if (date != null) {
				Certainty cirtainty = date.getCertainty();
				
				if (cirtainty != null) {
					value = cirtainty.toString();
				}
			}
		}
		
		return value;
	}
	
	/**
	 * Returns the latest qualifier type for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getLatestQualifierType("June 9, 2014") # .
	 */
	public static String getLatestQualifierType(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getLatestDate();
			
			if (date != null) {
				QualifierType qualifierType = date.getQualifierType();
				
				if (qualifierType != null) {
					value = qualifierType.toString();
				}
			}
		}
		
		return value;
	}
	
	/**
	 * Returns the latest qualifier value for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getLatestQualifierValue("June 9, 2014") # .
	 */
	public static String getLatestQualifierValue(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getLatestDate();
			
			if (date != null) {
				Integer qualifierValue = date.getQualifierValue();
				
				if (qualifierValue != null) {
					value = qualifierValue.toString();
				}
			}
		}
		
		return value;
	}

	/**
	 * Returns the latest qualifier unit for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getLatestQualifierUnit("June 9, 2014") # .
	 */
	public static String getLatestQualifierUnit(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			Date date = structuredDate.getLatestDate();
			
			if (date != null) {
				QualifierUnit qualifierUnit = date.getQualifierUnit();
				
				if (qualifierUnit != null) {
					value = qualifierUnit.toString();
				}
			}
		}
		
		return value;
	}
	
	/**
	 * Returns the latest scalar date for a display date string.
	 * 
	 * {talendTypes} string
	 *
	 * {Category} Structured Date
	 *
	 * {param} string("display date") input: The display date to parse.
	 *
	 * {example} getLatestScalarDate("June 9, 2014") # 2014-06-09T23:59:59Z.
	 */
	public static String getLatestScalarDate(String displayDate) {
		StructuredDate structuredDate = parse(displayDate).structuredDate;
		String value = "";
		
		if (structuredDate != null) {
			String scalarDate = structuredDate.getLatestScalarDate();
			
			if (scalarDate != null) {
				value = scalarDate;
			}
		}
		
		return value;
	}
	
	private static ParseResult parse(String displayDate) {
		ParseResult parseResult = getCachedParseResult(displayDate);
		
		if (parseResult == null) {
			// Parse the display date, and cache the result.

			boolean isValid = true;
			String errorMessage = null;
			StructuredDate structuredDate = null;
			
			try {
				structuredDate = StructuredDate.parse(displayDate);
			}
			catch(StructuredDateFormatException e) {
				isValid = false;
				errorMessage = e.getMessage();
			}
			
			if (structuredDate != null) {
				try {
					structuredDate.computeScalarValues();
				}
				catch(InvalidDateException e) {
					isValid = false;
					errorMessage = e.getMessage();
				}
			}
			
			parseResult = addCachedParseResult(displayDate, isValid, errorMessage, structuredDate);
		}
		
		return parseResult;
	}
	
	private static ParseResult getCachedParseResult(String displayDate) {
		ParseResult parseResult = null;
		
		if (StringUtils.equals(displayDate, cachedParseResult.displayDate)) {
			parseResult = cachedParseResult;
		}
		
		return parseResult;
	}
	
	private static ParseResult addCachedParseResult(String displayDate, boolean isValid, String errorMessage, StructuredDate structuredDate) {
		cachedParseResult.displayDate = displayDate;
		cachedParseResult.isValid = isValid;
		cachedParseResult.errorMessage = errorMessage;
		cachedParseResult.structuredDate = structuredDate;
		
		return cachedParseResult;
	}
	
	private static class ParseResult {
		public String displayDate = null;
		public boolean isValid = false;
		public String errorMessage = null;
		public StructuredDate structuredDate = null;
	}
	
	public static void main(String args[]) {
		// Tests
		
		String[] displayDates = new String[]{
				"1965-08-06", "1971-12", "2000", "June 9, 2014", "December 231, 1978", "foo bar"
		};
		
		for (String displayDate : displayDates) {
			System.out.println(displayDate);
			System.out.println(isValid(displayDate));
			System.out.println(getErrorMessage(displayDate));
			System.out.println(getEarliestSingleYear(displayDate));
			System.out.println(getEarliestSingleMonth(displayDate));
			System.out.println(getEarliestSingleDay(displayDate));
			System.out.println(getEarliestSingleEra(displayDate));
			System.out.println(getEarliestSingleCertainty(displayDate));
			System.out.println(getEarliestSingleQualifierType(displayDate));
			System.out.println(getEarliestSingleQualifierValue(displayDate));
			System.out.println(getEarliestSingleQualifierUnit(displayDate));
			System.out.println(getEarliestSingleScalarDate(displayDate));
			System.out.println(getLatestYear(displayDate));
			System.out.println(getLatestMonth(displayDate));
			System.out.println(getLatestDay(displayDate));
			System.out.println(getLatestEra(displayDate));
			System.out.println(getLatestCertainty(displayDate));
			System.out.println(getLatestQualifierType(displayDate));
			System.out.println(getLatestQualifierValue(displayDate));
			System.out.println(getLatestQualifierUnit(displayDate));
			System.out.println(getLatestScalarDate(displayDate));
			System.out.println();
		}
	}
}
