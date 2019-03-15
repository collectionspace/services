package org.collectionspace.services.structureddate.antlr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Stack;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.collectionspace.services.structureddate.Date;
import org.collectionspace.services.structureddate.DateUtils;
import org.collectionspace.services.structureddate.DeferredCenturyEndDate;
import org.collectionspace.services.structureddate.DeferredCenturyStartDate;
import org.collectionspace.services.structureddate.DeferredDate;
import org.collectionspace.services.structureddate.DeferredDecadeEndDate;
import org.collectionspace.services.structureddate.DeferredDecadeStartDate;
import org.collectionspace.services.structureddate.DeferredHalfCenturyEndDate;
import org.collectionspace.services.structureddate.DeferredHalfCenturyStartDate;
import org.collectionspace.services.structureddate.DeferredMillenniumEndDate;
import org.collectionspace.services.structureddate.DeferredMillenniumStartDate;
import org.collectionspace.services.structureddate.DeferredPartialCenturyEndDate;
import org.collectionspace.services.structureddate.DeferredPartialCenturyStartDate;
import org.collectionspace.services.structureddate.DeferredPartialDecadeEndDate;
import org.collectionspace.services.structureddate.DeferredPartialDecadeStartDate;
import org.collectionspace.services.structureddate.DeferredQuarterCenturyEndDate;
import org.collectionspace.services.structureddate.DeferredQuarterCenturyStartDate;
import org.collectionspace.services.structureddate.Era;
import org.collectionspace.services.structureddate.Part;
import org.collectionspace.services.structureddate.StructuredDateInternal;
import org.collectionspace.services.structureddate.StructuredDateEvaluator;
import org.collectionspace.services.structureddate.StructuredDateFormatException;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.AllOrPartOfContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.BeforeOrAfterDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.CenturyContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.CertainDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.DateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.DayFirstDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.DayOrYearFirstDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.DecadeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.DisplayDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.EraContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.HalfCenturyContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.HalfYearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.HyphenatedRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.InvMonthYearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.InvSeasonYearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.InvStrDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.InvStrDateEraLastDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.MillenniumContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.MonthContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.MonthInYearRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.MonthYearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NthCenturyRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NthContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NthHalfContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NthQuarterContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NthQuarterInYearRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NthQuarterYearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NumCenturyContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NumContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NumDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NumDayInMonthRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NumDayOfMonthContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NumDecadeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NumMonthContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NumYearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.PartOfContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.PartialCenturyContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.PartialDecadeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.PartialYearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.RomanDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.QuarterCenturyContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.QuarterInYearRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.QuarterYearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.RomanMonthContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.SeasonYearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.StrCenturyContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.StrDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.StrDayInMonthRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.StrMonthContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.StrSeasonContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.StrSeasonInYearRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.UncalibratedDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.UncertainDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.UnknownDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.YearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.YearSpanningWinterContext;

/**
 * A StructuredDateEvaluator that uses an ANTLR parser to parse the display date,
 * and an ANTLR listener to generate a structured date from the resulting parse
 * tree.
 */
public class ANTLRStructuredDateEvaluator extends StructuredDateBaseListener implements StructuredDateEvaluator {
	/**
	 * The result of the evaluation.
	 */
	protected StructuredDateInternal result;
	private final int BP_ZERO_YEAR = 1950;

	/**
	 * The operation stack. The parse listener methods that are implemented here
	 * pop input parameters off the stack, and push results back on to the stack.
	 */
	protected Stack<Object> stack;

	public ANTLRStructuredDateEvaluator() {

	}

	@Override
	public StructuredDateInternal evaluate(String displayDate) throws StructuredDateFormatException {
		stack = new Stack<Object>();

		result = new StructuredDateInternal();
		result.setDisplayDate(displayDate);

		// Instantiate a parser from the lowercased display date, so that parsing will be case insensitive 
		ANTLRInputStream inputStream = new ANTLRInputStream(displayDate.toLowerCase());
		StructuredDateLexer lexer = new StructuredDateLexer(inputStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		StructuredDateParser parser = new StructuredDateParser(tokenStream);

		// Don't try to recover from parse errors, just bail.
		parser.setErrorHandler(new BailErrorStrategy());

		// Don't print error messages to the console.
		parser.removeErrorListeners();

		// Generate our own custom error messages.
		parser.addParseListener(this);

		try {
			// Attempt to fulfill the oneDisplayDate rule of the grammar.
			parser.oneDisplayDate();
		}
		catch(ParseCancellationException e) {
			// ParseCancellationException is thrown by the BailErrorStrategy when there is a
			// parse error, with the underlying RecognitionException as the cause.
			RecognitionException re = (RecognitionException) e.getCause();

			throw new StructuredDateFormatException(getErrorMessage(re), re);
		}

		// The parsing was successful. Return the result.
		return result;
	}

	@Override
	public void exitDisplayDate(DisplayDateContext ctx) {
		if (ctx.exception != null) return;

		Date latestDate = (Date) stack.pop();
		Date earliestDate = (Date) stack.pop();

		if (earliestDate.getYear() != null || earliestDate.getYear() != null) {
			int compareResult = DateUtils.compareDates(earliestDate, latestDate);
			if (compareResult == 1) {
				Date temp;
				temp = earliestDate;
				earliestDate = latestDate;
				latestDate = temp;
	
				// Check to see if the dates were reversed AND calculated. If they were
				// Then this probably means the absolute earliestDate should have month and day as "1"
				// and the latestDate momth 12, day 31.
				if ((earliestDate.getMonth() == 12 && earliestDate.getDay() == 31) &&
					(latestDate.getMonth() == 1 && latestDate.getDay() == 1)) {
						earliestDate.setMonth(1);
						earliestDate.setDay(1);
						latestDate.setMonth(12);
						latestDate.setDay(31);
					}
			}
		}

		// If the earliest date and the latest date are the same, it's just a "single" date.
		// There's no need to have the latest, so set it to null.

		if (earliestDate.equals(latestDate)) {
			latestDate = null;
		}

		result.setEarliestSingleDate(earliestDate);
		result.setLatestDate(latestDate);
	}

	@Override
	public void exitBeforeOrAfterDate(BeforeOrAfterDateContext ctx) {
		if (ctx.exception != null) return;

		Date latestDate = (Date) stack.pop();
		Date earliestDate = (Date) stack.pop();

		// Set null eras to the default.

		if (earliestDate.getEra() == null) {
			earliestDate.setEra(Date.DEFAULT_ERA);
		}

		if (latestDate.getEra() == null) {
			latestDate.setEra(Date.DEFAULT_ERA);
		}

		// Finalize any deferred calculations.

		if (latestDate instanceof DeferredDate) {
			((DeferredDate) latestDate).resolveDate();
		}

		if (earliestDate instanceof DeferredDate) {
			((DeferredDate) earliestDate).resolveDate();
		}

		// Calculate the earliest date or end date.

		if (ctx.BEFORE() != null) {
			latestDate = earliestDate;
			earliestDate = DateUtils.getEarliestBeforeDate(earliestDate, latestDate);
		}
		else if (ctx.AFTER() != null) {
			earliestDate = latestDate;
			latestDate = DateUtils.getLatestAfterDate(earliestDate, latestDate);
		}

		stack.push(earliestDate);
		stack.push(latestDate);
	}

	@Override
	public void exitUncertainDate(UncertainDateContext ctx) {
		if (ctx.exception != null) return;

		Date latestDate = (Date) stack.pop();
		Date earliestDate = (Date) stack.pop();


		int earliestInterval = DateUtils.getCircaIntervalYears(earliestDate.getYear(), earliestDate.getEra());
		int latestInterval = DateUtils.getCircaIntervalYears(latestDate.getYear(), latestDate.getEra());

		// Express the circa interval as a qualifier.	

 		// stack.push(earliestDate.withQualifier(QualifierType.MINUS, earliestInterval, QualifierUnit.YEARS));	
		// stack.push(latestDate.withQualifier(QualifierType.PLUS, latestInterval, QualifierUnit.YEARS));	

 		// OR:	
		 
		// Express the circa interval as an offset calculated into the year.

		DateUtils.subtractYears(earliestDate, earliestInterval);
		DateUtils.addYears(latestDate, latestInterval);

		stack.push(earliestDate);
		stack.push(latestDate);
	}

	@Override
	public void exitCertainDate(CertainDateContext ctx) {
		if (ctx.exception != null) return;

		Date latestDate = (Date) stack.pop();
		Date earliestDate = (Date) stack.pop();

		// Set null eras to the default.

		if (earliestDate.getEra() == null) {
			earliestDate.setEra(Date.DEFAULT_ERA);
		}

		if (latestDate.getEra() == null) {
			latestDate.setEra(Date.DEFAULT_ERA);
		}

		// Finalize any deferred calculations.

		if (latestDate instanceof DeferredDate) {
			((DeferredDate) latestDate).resolveDate();
		}

		if (earliestDate instanceof DeferredDate) {
			((DeferredDate) earliestDate).resolveDate();
		}

		stack.push(earliestDate);
		stack.push(latestDate);
	}

	@Override
	public void exitHyphenatedRange(HyphenatedRangeContext ctx) {
		if (ctx.exception != null) return;

		Date latestEndDate = (Date) stack.pop();
		stack.pop(); // latestStartDate
		stack.pop(); // earliestEndDate
		Date earliestStartDate = (Date) stack.pop();

		// If no era was explicitly specified for the first date,
		// make it inherit the era of the second date.

		if (earliestStartDate.getEra() == null && latestEndDate.getEra() != null) {
			earliestStartDate.setEra(latestEndDate.getEra());
		}

		// Finalize any deferred calculations.

		if (earliestStartDate instanceof DeferredDate) {
			((DeferredDate) earliestStartDate).resolveDate();
		}

		if (latestEndDate instanceof DeferredDate) {
			((DeferredDate) latestEndDate).resolveDate();
		}

		stack.push(earliestStartDate);
		stack.push(latestEndDate);
	}

	@Override
	public void exitNthCenturyRange(NthCenturyRangeContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer endN = (Integer) stack.pop();
		Part endPart = (Part) stack.pop();
		Integer startN = (Integer) stack.pop();
		Part startPart = (Part) stack.pop();

		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		int startYear = DateUtils.nthCenturyToYear(startN);
		int endYear = DateUtils.nthCenturyToYear(endN);

		stack.push(startPart == null ? DateUtils.getCenturyStartDate(startYear, era) : DateUtils.getPartialCenturyStartDate(startYear, startPart, era));
		stack.push(startPart == null ? DateUtils.getCenturyEndDate(startYear, era) : DateUtils.getPartialCenturyEndDate(startYear, startPart, era));
		stack.push(endPart == null ? DateUtils.getCenturyStartDate(endYear, era) : DateUtils.getPartialCenturyStartDate(endYear, endPart, era));
		stack.push(endPart == null ? DateUtils.getCenturyEndDate(endYear, era) : DateUtils.getPartialCenturyEndDate(endYear, endPart, era));
	}

	@Override
	public void exitMonthInYearRange(MonthInYearRangeContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer numMonthEnd = (Integer) stack.pop();
		Integer numMonthStart = (Integer) stack.pop();

		stack.push(new Date(year, numMonthStart, 1, era));
		stack.push(new Date(year, numMonthStart, DateUtils.getDaysInMonth(numMonthStart, year, era), era));
		stack.push(new Date(year, numMonthEnd, 1, era));
		stack.push(new Date(year, numMonthEnd, DateUtils.getDaysInMonth(numMonthEnd, year, era), era));
	}

	@Override
	public void exitQuarterInYearRange(QuarterInYearRangeContext ctx) {
		if (ctx.exception != null) return;

		Era era = (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer lastQuarter = (Integer) stack.pop();
		Integer firstQuarter = (Integer) stack.pop();

		stack.push(DateUtils.getQuarterYearStartDate(firstQuarter, year).withEra(era));
		stack.push(DateUtils.getQuarterYearEndDate(firstQuarter, year, era).withEra(era));
		stack.push(DateUtils.getQuarterYearStartDate(lastQuarter, year).withEra(era));
		stack.push(DateUtils.getQuarterYearEndDate(lastQuarter, year, era).withEra(era));
	}

	@Override
	public void exitStrDayInMonthRange(StrDayInMonthRangeContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer dayOfMonthEnd = (Integer) stack.pop();
		Integer dayOfMonthStart = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();

		stack.push(new Date(year, numMonth, dayOfMonthStart, era));
		stack.push(new Date(year, numMonth, dayOfMonthStart, era));
		stack.push(new Date(year, numMonth, dayOfMonthEnd, era));
		stack.push(new Date(year, numMonth, dayOfMonthEnd, era));
	}

	@Override
	public void exitNumDayInMonthRange(NumDayInMonthRangeContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer num1 = (Integer) stack.pop();
		Integer num2 = (Integer) stack.pop();
		Integer num3 = (Integer) stack.pop();
		Integer num4 = (Integer) stack.pop();

		/* We can distinguish whether it is M/D-D/Y (Case 1) or M/Y-M/Y (Case 2) by checking if
			The num1, num4, num2 and num1, num4, num3 are valid dates, since this would equate to
			a set of two ranges. For examples: 04/13-19/1995 would be 04/13/1995-04/19/1995. If both these
			dates are valid, we know that it shouldn't be interpreted as 04/01/13 - 19/31/1995 since these arent valid dates!
		*/

		Integer lateYear = num1;
		Integer earlyMonth = num4;
		Integer dayOfMonthEnd = num2;
		Integer dayOfMonthStart = num3;

		if (DateUtils.isValidDate(num1, num4, num2, era) && DateUtils.isValidDate(num1, num4, num3, era)) {
			// No need to alter the arguments, so just push to the stack
			stack.push(new Date(lateYear, earlyMonth, dayOfMonthStart, era));
			stack.push(new Date(lateYear, earlyMonth, dayOfMonthStart, era));
			stack.push(new Date(lateYear, earlyMonth, dayOfMonthEnd, era));
			stack.push(new Date(lateYear, earlyMonth, dayOfMonthEnd, era));

		} else {
			// Separated these by case, since it makes the code more legible
			Integer latestMonth = num2;
			Integer earliestYear = num3;

			stack.push(new Date(earliestYear, earlyMonth, 1, era)); // Earliest Early Date
			stack.push(new Date(earliestYear, earlyMonth, DateUtils.getDaysInMonth(earlyMonth, earliestYear, era), era)); // Latest Early Date
			stack.push(new Date(lateYear, latestMonth, 1, era)); // Earliest Latest Date
			stack.push(new Date(lateYear, latestMonth, DateUtils.getDaysInMonth(latestMonth, lateYear, era), era)); // Latest Late Date
			
		}
	}

	@Override
	public void exitDate(DateContext ctx) {
		if (ctx.exception != null) return;

		// Expect the canonical year-month-day-era ordering
		// to be on the stack.

		Era era = (stack.size() == 3) ? null : (Era) stack.pop();
		Integer dayOfMonth = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();
		Integer year = (Integer) stack.pop();

		// For the latest date we could either return null, or a copy of the earliest date,
		// since the UI doesn't care. Use a copy of the earliest date, since it makes
		// things easier here if we don't have to test for null up the tree.

		stack.push(new Date(year, numMonth, dayOfMonth, era));
		stack.push(new Date(year, numMonth, dayOfMonth, era));
	}

	@Override
	public void exitNumDate(NumDateContext ctx) {
		if (ctx.exception != null) return;

		// This could either be year-month-day, or
		// month-day-year. Try to determine which,
		// and reorder the stack into the canonical
		// year-month-day-era ordering.

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer num3 = (Integer) stack.pop();
		Integer num2 = (Integer) stack.pop();
		Integer num1 = (Integer) stack.pop();

		// Default to a month-day-year interpretation.

		int numMonth = num1;
		int dayOfMonth = num2;
		int year = num3;

		if (DateUtils.isValidDate(num3, num1, num2, era)) {
			// Interpreting as month-day-year produces a valid date. Go with it.
		}
		else if (DateUtils.isValidDate(num1, num2, num3, era)) {
			// Interpreting as month-day-year doesn't produce a valid date, but
			// year-month-day does. Go with year-month-day.

			year = num1;
			numMonth = num2;
			dayOfMonth = num3;
		}

		stack.push(year);
		stack.push(numMonth);
		stack.push(dayOfMonth);
		stack.push(era);
	}

	@Override
	public void exitStrDate(StrDateContext ctx) {
		if (ctx.exception != null) return;

		// Reorder the stack into a canonical ordering,
		// year-month-day-era.

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer dayOfMonth = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();

		stack.push(year);
		stack.push(numMonth);
		stack.push(dayOfMonth);
		stack.push(era);
	}

	@Override
	public void exitInvStrDate(InvStrDateContext ctx) {
		if (ctx.exception != null) return;

		// Reorder the stack into a canonical ordering,
		// year-month-day-era.

		Integer dayOfMonth = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();
		Integer year = (Integer) stack.pop();
		Era era = (ctx.era() == null) ? null : (Era) stack.pop();

		stack.push(year);
		stack.push(numMonth);
		stack.push(dayOfMonth);
		stack.push(era);
	}

	@Override
	public void exitDayFirstDate(DayFirstDateContext ctx) {
		if (ctx.exception != null) return ;
		
		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer month = (Integer) stack.pop();
		Integer dayOfMonth = (Integer) stack.pop();

		stack.push(year);
		stack.push(month);
		stack.push(dayOfMonth);
		stack.push(era);
	}

	@Override
	public void exitDayOrYearFirstDate(DayOrYearFirstDateContext ctx) {
		if (ctx.exception != null) return;

		Era era = null;
		Integer num2 = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();
		Integer num1 = (Integer) stack.pop();

		Integer year = num1;
		Integer dayOfMonth = num2;

 		if (DateUtils.isValidDate(num1, numMonth, num2, era)) {
			// The first number is a year. Already correct
		} else if (DateUtils.isValidDate(num2, numMonth, num1, era)) {
			// The second number is a year.
			year = num2;
			dayOfMonth = num1;
		}

		stack.push(year);
		stack.push(numMonth);
		stack.push(dayOfMonth);

		if (dayOfMonth > 31 || dayOfMonth <= 0) {
			throw new StructuredDateFormatException("unexpected day of month '" + Integer.toString(dayOfMonth) + "'");
		}
		if (year == 0) {
			throw new StructuredDateFormatException("unexpected year '" + Integer.toString(year) + "'");
		}
	}

	@Override
	public void exitInvStrDateEraLastDate(InvStrDateEraLastDateContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer dayOfMonth = (Integer) stack.pop();
		Integer month = (Integer) stack.pop();
		Integer year = (Integer) stack.pop();

		stack.push(year);
		stack.push(month);
		stack.push(dayOfMonth);
		stack.push(era);
	}

	@Override
	public void exitMonth(MonthContext ctx) {
		if (ctx.exception != null) return;

		Era era = (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();

		stack.push(new Date(year, numMonth, 1, era));
		stack.push(new Date(year, numMonth, DateUtils.getDaysInMonth(numMonth, year, era), era));
	}

	@Override
	public void exitMonthYear(MonthYearContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();

		stack.push(era);
	}

	@Override
	public void exitInvMonthYear(InvMonthYearContext ctx) {
		if (ctx.exception != null) return;

		// Invert the arguments.

		Integer numMonth = (Integer) stack.pop();
		Integer year = (Integer) stack.pop();
		Era era = (ctx.era() == null) ? null : (Era) stack.pop();

		stack.push(numMonth);
		stack.push(year);
		stack.push(era);
	}

	@Override
	public void exitYearSpanningWinter(YearSpanningWinterContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer endYear = (Integer) stack.pop();
		Integer startYear = (Integer) stack.pop();

		stack.push(new Date(startYear, 12, 1).withEra(era));
		stack.push(DateUtils.getQuarterYearEndDate(1, endYear, era).withEra(era));
	}

	@Override
	public void exitPartialYear(PartialYearContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Part part = (Part) stack.pop();

		stack.push(DateUtils.getPartialYearStartDate(part, year).withEra(era));
		stack.push(DateUtils.getPartialYearEndDate(part, year, era).withEra(era));
	}

	@Override
	public void exitQuarterYear(QuarterYearContext ctx) {
		if (ctx.exception != null) return;

		Era era = (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer quarter = (Integer) stack.pop();

		stack.push(DateUtils.getQuarterYearStartDate(quarter, year).withEra(era));
		stack.push(DateUtils.getQuarterYearEndDate(quarter, year, era).withEra(era));
	}

	@Override
	public void exitHalfYear(HalfYearContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer half = (Integer) stack.pop();

		stack.push(DateUtils.getHalfYearStartDate(half, year).withEra(era));
		stack.push(DateUtils.getHalfYearEndDate(half, year, era).withEra(era));
	}

	@Override
	public void exitInvSeasonYear(InvSeasonYearContext ctx) {
		if (ctx.exception != null) return;

		// Invert the arguments.

		Integer quarter = (Integer) stack.pop();
		Integer year = (Integer) stack.pop();
		Era era = (ctx.era() == null) ? null : (Era) stack.pop();

		stack.push(quarter);
		stack.push(year);
		stack.push(era);
	}

	@Override 
	public void exitSeasonYear(SeasonYearContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		stack.push(era);

	}

	@Override
	public void exitYear(YearContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer year = (Integer) stack.pop();

		stack.push(new Date(year, 1, 1, era));
		stack.push(new Date(year, 12, 31, era));
	}

	@Override
	public void exitPartialDecade(PartialDecadeContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Part part = (Part) stack.pop();

		if (era != null) {
			// If the era was explicitly specified, the start and end years
			// may be calculated now.

			stack.push(DateUtils.getPartialDecadeStartDate(year, part, era));
			stack.push(DateUtils.getPartialDecadeEndDate(year, part, era));
		}
		else {
			// If the era was not explicitly specified, the start and end years
			// can't be calculated yet. The calculation must be deferred until
			// later. For example, this partial decade may be the start of a hyphenated
			// range, where the era will be inherited from the era of the end of
			// the range; this era won't be known until farther up the parse tree,
			// when both sides of the range will have been parsed.

			stack.push(new DeferredPartialDecadeStartDate(year, part));
			stack.push(new DeferredPartialDecadeEndDate(year, part));
		}
	}

	@Override
	public void exitDecade(DecadeContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer year = (Integer) stack.pop();

		// Calculate the start and end year of the decade, which depends on the era.

		if (era != null) {
			// If the era was explicitly specified, the start and end years
			// may be calculated now.

			stack.push(DateUtils.getDecadeStartDate(year, era));
			stack.push(DateUtils.getDecadeEndDate(year, era));
		}
		else {
			// If the era was not explicitly specified, the start and end years
			// can't be calculated yet. The calculation must be deferred until
			// later. For example, this decade may be the start of a hyphenated
			// range, where the era will be inherited from the era of the end of
			// the range; this era won't be known until farther up the parse tree,
			// when both sides of the range will have been parsed.

			stack.push(new DeferredDecadeStartDate(year));
			stack.push(new DeferredDecadeEndDate(year));
		}
	}

	@Override
	public void exitPartialCentury(PartialCenturyContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Part part = (Part) stack.pop();

		if (era != null) {
			// If the era was explicitly specified, the start and end years
			// may be calculated now.

			stack.push(DateUtils.getPartialCenturyStartDate(year, part, era));
			stack.push(DateUtils.getPartialCenturyEndDate(year, part, era));
		}
		else {
			// If the era was not explicitly specified, the start and end years
			// can't be calculated yet. The calculation must be deferred until
			// later. For example, this partial century may be the start of a hyphenated
			// range, where the era will be inherited from the era of the end of
			// the range; this era won't be known until farther up the parse tree,
			// when both sides of the range will have been parsed.

			stack.push(new DeferredPartialCenturyStartDate(year, part));
			stack.push(new DeferredPartialCenturyEndDate(year, part));
		}
	}

	@Override
	public void exitQuarterCentury(QuarterCenturyContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer quarter = (Integer) stack.pop();

		if (era != null) {
			// If the era was explicitly specified, the start and end years
			// may be calculated now.

			stack.push(DateUtils.getQuarterCenturyStartDate(year, quarter, era));
			stack.push(DateUtils.getQuarterCenturyEndDate(year, quarter, era));
		}
		else {
			// If the era was not explicitly specified, the start and end years
			// can't be calculated yet. The calculation must be deferred until
			// later. For example, this century may be the start of a hyphenated
			// range, where the era will be inherited from the era of the end of
			// the range; this era won't be known until farther up the parse tree,
			// when both sides of the range will have been parsed.

			stack.push(new DeferredQuarterCenturyStartDate(year, quarter));
			stack.push(new DeferredQuarterCenturyEndDate(year, quarter));
		}
	}

	@Override
	public void exitHalfCentury(HalfCenturyContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer half = (Integer) stack.pop();

		if (era != null) {
			// If the era was explicitly specified, the start and end years
			// may be calculated now.

			stack.push(DateUtils.getHalfCenturyStartDate(year, half, era));
			stack.push(DateUtils.getHalfCenturyEndDate(year, half, era));
		}
		else {
			// If the era was not explicitly specified, the start and end years
			// can't be calculated yet. The calculation must be deferred until
			// later. For example, this half century may be the start of a hyphenated
			// range, where the era will be inherited from the era of the end of
			// the range; this era won't be known until farther up the parse tree,
			// when both sides of the range will have been parsed.

			stack.push(new DeferredHalfCenturyStartDate(year, half));
			stack.push(new DeferredHalfCenturyEndDate(year, half));
		}
	}

	@Override
	public void exitCentury(CenturyContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer year = (Integer) stack.pop();

		if (era != null) {
			// If the era was explicitly specified, the start and end years
			// may be calculated now.

			stack.push(DateUtils.getCenturyStartDate(year, era));
			stack.push(DateUtils.getCenturyEndDate(year, era));
		}
		else {
			// If the era was not explicitly specified, the start and end years
			// can't be calculated yet. The calculation must be deferred until
			// later. For example, this quarter century may be the start of a hyphenated
			// range, where the era will be inherited from the era of the end of
			// the range; this era won't be known until farther up the parse tree,
			// when both sides of the range will have been parsed.

			stack.push(new DeferredCenturyStartDate(year));
			stack.push(new DeferredCenturyEndDate(year));
		}
	}

	@Override
	public void exitMillennium(MillenniumContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer n = (Integer) stack.pop();

		if (era != null) {
			// If the era was explicitly specified, the start and end years
			// may be calculated now.

			stack.push(DateUtils.getMillenniumStartDate(n, era));
			stack.push(DateUtils.getMillenniumEndDate(n, era));
		}
		else {
			// If the era was not explicitly specified, the start and end years
			// can't be calculated yet. The calculation must be deferred until
			// later. For example, this millennium may be the start of a hyphenated
			// range, where the era will be inherited from the era of the end of
			// the range; this era won't be known until farther up the parse tree,
			// when both sides of the range will have been parsed.

			stack.push(new DeferredMillenniumStartDate(n));
			stack.push(new DeferredMillenniumEndDate(n));
		}
	}

	@Override
	public void exitStrCentury(StrCenturyContext ctx) {
		if (ctx.exception != null) return;

		Integer n = (Integer) stack.pop();

		// Convert the nth number to a year number,
		// and push on the stack.

		Integer year = DateUtils.nthCenturyToYear(n);

		stack.push(year);
	}

	@Override
	public void exitNumCentury(NumCenturyContext ctx) {
		if (ctx.exception != null) return;

		// Convert the string to a number,
		// and push on the stack.

		Integer year = new Integer(stripEndLetters(ctx.HUNDREDS().getText()));

		if (year == 0) {
			throw new StructuredDateFormatException("unexpected century '" + ctx.HUNDREDS().getText() + "'");
		}

		stack.push(year);
	}

	@Override
	public void exitNumDecade(NumDecadeContext ctx) {
		if (ctx.exception != null) return;

		// Convert the string to a number,
		// and push on the stack.

		Integer year = new Integer(stripEndLetters(ctx.TENS().getText()));

		if (year == 0) {
			throw new StructuredDateFormatException("unexpected decade '" + ctx.TENS().getText() + "'");
		}

		stack.push(year);
	}

	@Override
	public void exitNumYear(NumYearContext ctx) {
		if (ctx.exception != null) return;

		// Convert the string to a number,
		// and push on the stack.

		Integer year = new Integer(ctx.getText().replaceAll(",", ""));

		if (year == 0) {
			throw new StructuredDateFormatException("unexpected year '" + ctx.NUMBER().getText() + "'");
		}

		stack.push(year);
	}

	@Override
	public void exitNumMonth(NumMonthContext ctx) {
		if (ctx.exception != null) return;

		// Convert the string a number,
		// and push on the stack.

		Integer month = new Integer(ctx.NUMBER().getText());

		if (month < 1 || month > 12) {
			throw new StructuredDateFormatException("unexpected month '" + ctx.NUMBER().getText() + "'");
		}

		stack.push(month);
	}

	@Override
	public void exitNthHalf(NthHalfContext ctx) {
		if (ctx.exception != null) return;

		// Convert LAST to a number (the last half
		// is the 2nd). If this rule matched the
		// alternative with nth instead of LAST,
		// the nth handler will already have pushed
		// a number on the stack.

		if (ctx.LAST() != null) {
			stack.push(new Integer(2));
		}

		// Check for a valid half.

		Integer n = (Integer) stack.peek();

		if (n < 1 || n > 2) {
			throw new StructuredDateFormatException("unexpected half '" + n + "'");
		}
	}
	

	@Override
	public void exitNthQuarterInYearRange(NthQuarterInYearRangeContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();

		stack.push(era);
	}

	@Override
	public void exitStrSeasonInYearRange(StrSeasonInYearRangeContext ctx) {
		if (ctx.exception != null) return;

		Era era = (ctx.era() == null) ? null : (Era) stack.pop();

		stack.push(era);

	}

	@Override
	public void exitNthQuarterYear(NthQuarterYearContext ctx) {
		
		Era era = (ctx.era() == null) ? null : (Era) stack.pop();

		stack.push(era);
	}

	@Override
	public void exitNthQuarter(NthQuarterContext ctx) {
		if (ctx.exception != null) return;

		// Convert LAST to a number (the last quarter
		// is the 4th). If this rule matched the
		// alternative with nth instead of LAST,
		// the nth handler will already have pushed
		// a number on the stack.

		if (ctx.LAST() != null) {
			stack.push(new Integer(4));
		}

		// Check for a valid quarter.

		Integer n = (Integer) stack.peek();

		if (n < 1 || n > 4) {
			throw new StructuredDateFormatException("unexpected quarter '" + n + "'");
		}
	}

	@Override
	public void exitNth(NthContext ctx) {
		if (ctx.exception != null) return;

		// Convert the string to a number,
		// and push on the stack.

		Integer n = null;

		if (ctx.NTHSTR() != null) {
			n = new Integer(stripEndLetters(ctx.NTHSTR().getText()));
		}
		else if (ctx.FIRST() != null) {
			n = 1;
		}
		else if (ctx.SECOND() != null) {
			n = 2;
		}
		else if (ctx.THIRD() != null) {
			n = 3;
		}
		else if (ctx.FOURTH() != null) {
			n = 4;
		}

		stack.push(n);
	}

	@Override
	public void exitStrMonth(StrMonthContext ctx) {
		if (ctx.exception != null) return;

		// Convert the month name to a number,
		// and push on the stack.

		TerminalNode monthNode = ctx.MONTH();

		if (monthNode == null) {
			monthNode = ctx.SHORTMONTH();
		}

		String monthStr = monthNode.getText();

		stack.push(DateUtils.getMonthByName(monthStr));
	}

	@Override
	public void exitStrSeason(StrSeasonContext ctx) {
		if (ctx.exception != null) return;

		// Convert the season to a quarter number,
		// and push on the stack.

		Integer quarter = null;

		if (ctx.WINTER() != null) {
			quarter = 1;
		}
		else if (ctx.SPRING() != null) {
			quarter = 2;
		}
		else if (ctx.SUMMER() != null) {
			quarter = 3;
		}
		else if (ctx.FALL() != null) {
			quarter = 4;
		}

		stack.push(quarter);
	}

	@Override
	public void exitAllOrPartOf(AllOrPartOfContext ctx) {
		if (ctx.exception != null) return;

		// If a part was specified, it will have been
		// pushed on the stack in exitPartOf(). If not,
		// push null on the stack.

		if (ctx.partOf() == null) {
			stack.push(null);
		}
	}

	@Override
	public void exitPartOf(PartOfContext ctx) {
		if (ctx.exception != null) return;

		// Convert the token to a Part,
		// and push on the stack.

		Part part = null;

		if (ctx.EARLY() != null) {
			part = Part.EARLY;
		}
		else if (ctx.MIDDLE() != null) {
			part = Part.MIDDLE;
		}
		else if (ctx.LATE() != null) {
			part = Part.LATE;
		}

		stack.push(part);
	}

	@Override
	public void exitEra(EraContext ctx) {
		if (ctx.exception != null) return;

		// Convert the token to an Era,
		// and push on the stack.

		Era era = null;

		if (ctx.BC() != null) {
			era = Era.BCE;
		}
		else if (ctx.AD() != null) {
			era = Era.CE;
		}

		stack.push(era);
	}

	@Override
	public void exitNumDayOfMonth(NumDayOfMonthContext ctx) {
		if (ctx.exception != null) return;

		// Convert the numeric string to an Integer,
		// and push on the stack.

		Integer dayOfMonth = new Integer(ctx.NUMBER().getText());

		if (dayOfMonth == 0 || dayOfMonth > 31) {
			throw new StructuredDateFormatException("unexpected day of month '" + ctx.NUMBER().getText() + "'");
		}

		stack.push(dayOfMonth);
	}

	@Override
	public void exitNum(NumContext ctx) {
		if (ctx.exception != null) return;

		// Convert the numeric string to an Integer,
		// and push on the stack.

		Integer num = new Integer(ctx.getText().replaceAll(",", ""));

		stack.push(num);
	}

	@Override
	public void exitRomanMonth(RomanMonthContext ctx) {
		int num = DateUtils.romanToDecimal(ctx.ROMANMONTH().getText());

		stack.push(num);
	}

	@Override
	public void exitRomanDate(RomanDateContext ctx) {
		if (ctx.exception != null) return;
		System.out.println("I am going in here");
		
		Era era = (ctx.era() == null) ? null : (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer month = (Integer) stack.pop();
		Integer day = (Integer) stack.pop();

		stack.push(year);
		stack.push(month);
		stack.push(day);
		stack.push(era);
	}

	@Override
	public void exitUnknownDate(UnknownDateContext ctx) {
		if (ctx.exception != null) return;

		// Dummy dates
		stack.push(new Date());
		stack.push(new Date());
	}

	public void exitUncalibratedDate(UncalibratedDateContext ctx) {
		if (ctx.exception != null) return;

		Integer adjustmentDate = (Integer) stack.pop();
		Integer mainYear = (Integer) stack.pop(); 

		Integer earliestYear = (BP_ZERO_YEAR - mainYear) - adjustmentDate;
		Integer latestYear = (BP_ZERO_YEAR - mainYear) + adjustmentDate;

		// If negative, then BC, else AD
		Era earliestEra = earliestYear < 0 ? Era.BCE : Era.CE;
		Era latestEra = latestYear < 0 ? Era.BCE : Era.CE;

		stack.push(new Date(Math.abs(earliestYear), 1, 1, earliestEra)); // Earliest Early Date
		stack.push(new Date(Math.abs(latestYear), 12, DateUtils.getDaysInMonth(12, Math.abs(latestYear), latestEra), latestEra)); // Latest Late Date

	}

	protected String getErrorMessage(RecognitionException re) {
		String message = "";

		Parser recognizer = (Parser) re.getRecognizer();
		TokenStream tokens = recognizer.getInputStream();

		if (re instanceof NoViableAltException) {
			NoViableAltException e = (NoViableAltException) re;
			Token startToken = e.getStartToken();
			String input = (startToken.getType() == Token.EOF ) ? "end of text" : quote(tokens.getText(startToken, e.getOffendingToken()));

			message = "no viable date format found at " + input;
		}
		else if (re instanceof InputMismatchException) {
			InputMismatchException e = (InputMismatchException) re;
			message = "did not expect " + getTokenDisplayString(e.getOffendingToken()) + " while looking for " +
			          e.getExpectedTokens().toString(recognizer.getTokenNames());
		}
		else if (re instanceof FailedPredicateException) {
			FailedPredicateException e = (FailedPredicateException) re;
			String ruleName = recognizer.getRuleNames()[recognizer.getContext().getRuleIndex()];

			message = "failed predicate " + ruleName + ": " + e.getMessage();
		}

		return message;
	}

	protected String quote(String text) {
		return "'" + text + "'";
	}

	protected String getTokenDisplayString(Token token) {
		String string;

		if (token == null) {
			string = "[no token]";
		}
		else {
			String text = token.getText();

			if (text == null) {
				if (token.getType() == Token.EOF ) {
					string = "end of text";
				}
				else {
					string = "[" + token.getType() + "]";
				}
			}
			else {
				string = quote(text);
			}
		}

		return string;
	}

	protected String stripEndLetters(String input) {
		return input.replaceAll("[^\\d]+$", "");
	}

	public static void main(String[] args) {
		StructuredDateEvaluator evaluator = new ANTLRStructuredDateEvaluator();

		for (String displayDate : args) {
			try {
				evaluator.evaluate(displayDate);
			} catch (StructuredDateFormatException e) {
				e.printStackTrace();
			}
		}
	}
}
