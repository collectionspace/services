package org.collectionspace.services.structureddate.antlr;

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
import org.collectionspace.services.structureddate.Era;
import org.collectionspace.services.structureddate.QualifierType;
import org.collectionspace.services.structureddate.QualifierUnit;
import org.collectionspace.services.structureddate.StructuredDate;
import org.collectionspace.services.structureddate.StructuredDateEvaluator;
import org.collectionspace.services.structureddate.StructuredDateFormatException;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.DateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.DisplayDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.EraContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.HalfYearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.HyphenatedRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.InvMonthYearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.InvSeasonYearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.InvStrDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.MonthContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.MonthInYearRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NthContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NthHalfContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NthQuarterContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NumDayInMonthRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NumDayOfMonthContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NumMonthContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NumYearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.QuarterInYearRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.QuarterYearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.StrDayInMonthRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.StrMonthContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.StrSeasonContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.UncertainDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.YearContext;

/**
 * A StructuredDateEvaluator that uses an ANTLR parser to parse the display date,
 * and an ANTLR listener to generate a structured date from the resulting parse
 * tree.
 */
public class ANTLRStructuredDateEvaluator extends StructuredDateBaseListener implements StructuredDateEvaluator {
	public static final int FIRST_MONTH = 1;
	public static final int FIRST_DAY_OF_FIRST_MONTH = 1;
	public static final int LAST_MONTH = 12;
	public static final int LAST_DAY_OF_LAST_MONTH = 31;
	
	/**
	 * The result of the evaluation.
	 */
	protected StructuredDate result;
	
	/**
	 * The operation stack. The parse listener methods that are implemented here
	 * pop input parameters off the stack, and push results back on to the stack.
	 */
	protected Stack<Object> stack;
	
	public ANTLRStructuredDateEvaluator() {

	}

	@Override
	public StructuredDate evaluate(String displayDate) throws StructuredDateFormatException {
		stack = new Stack<Object>();

		result = new StructuredDate();
		result.setDisplayDate(displayDate);

		// Instantiate a parser from the lowercased display date, so that parsing will be
		// case insensitive.
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

		// The UI may not know what to do with null eras,
		// so explicitly set them to the default.
		
		if (earliestDate.getEra() == null) {
			earliestDate.setEra(Date.DEFAULT_ERA);
		}
		
		if (latestDate.getEra() == null) {
			latestDate.setEra(Date.DEFAULT_ERA);
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
	public void exitUncertainDate(UncertainDateContext ctx) {
		if (ctx.exception != null) return;

		Date latestDate = (Date) stack.pop();
		Date earliestDate = (Date) stack.pop();
		
		int earliestInterval = DateUtils.getCircaIntervalYears(earliestDate.getYear(), earliestDate.getEra());
		int latestInterval = DateUtils.getCircaIntervalYears(latestDate.getYear(), latestDate.getEra());
		
		stack.push(earliestDate.withQualifier(QualifierType.MINUS, earliestInterval, QualifierUnit.YEARS));
		stack.push(latestDate.withQualifier(QualifierType.PLUS, latestInterval, QualifierUnit.YEARS));	
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

		if (earliestStartDate.getEra() == null) {
			earliestStartDate.setEra(latestEndDate.getEra());
		}
		
		stack.push(earliestStartDate);
		stack.push(latestEndDate);
	}

	@Override
	public void exitMonthInYearRange(MonthInYearRangeContext ctx) {
		if (ctx.exception != null) return;

		Era era = (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer numMonthEnd = (Integer) stack.pop();
		Integer numMonthStart = (Integer) stack.pop();
		
		stack.push(new Date(year, numMonthStart, 1).withEra(era));
		stack.push(new Date(year, numMonthStart, DateUtils.getDaysInMonth(numMonthStart, year)).withEra(era));
		stack.push(new Date(year, numMonthEnd, 1).withEra(era));
		stack.push(new Date(year, numMonthEnd, DateUtils.getDaysInMonth(numMonthEnd, year)).withEra(era));		
	}
	
	@Override
	public void exitQuarterInYearRange(QuarterInYearRangeContext ctx) {
		if (ctx.exception != null) return;

		Era era = (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer lastQuarter = (Integer) stack.pop();
		Integer firstQuarter = (Integer) stack.pop();
		
		stack.push(DateUtils.getQuarterYearStartDate(year, firstQuarter).withEra(era));
		stack.push(DateUtils.getQuarterYearEndDate(year, firstQuarter).withEra(era));
		stack.push(DateUtils.getQuarterYearStartDate(year, lastQuarter).withEra(era));
		stack.push(DateUtils.getQuarterYearEndDate(year, lastQuarter).withEra(era));
	}

	@Override
	public void exitStrDayInMonthRange(StrDayInMonthRangeContext ctx) {
		if (ctx.exception != null) return;

		Era era = (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer dayOfMonthEnd = (Integer) stack.pop();
		Integer dayOfMonthStart = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();
		
		stack.push(new Date(year, numMonth, dayOfMonthStart).withEra(era));
		stack.push(new Date(year, numMonth, dayOfMonthStart).withEra(era));		
		stack.push(new Date(year, numMonth, dayOfMonthEnd).withEra(era));
		stack.push(new Date(year, numMonth, dayOfMonthEnd).withEra(era));		
	}
	
	@Override
	public void exitNumDayInMonthRange(NumDayInMonthRangeContext ctx) {
		if (ctx.exception != null) return;

		Era era = (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer dayOfMonthEnd = (Integer) stack.pop();
		Integer dayOfMonthStart = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();
		
		stack.push(new Date(year, numMonth, dayOfMonthStart).withEra(era));
		stack.push(new Date(year, numMonth, dayOfMonthStart).withEra(era));
		stack.push(new Date(year, numMonth, dayOfMonthEnd).withEra(era));		
		stack.push(new Date(year, numMonth, dayOfMonthEnd).withEra(era));		
	}
	
	@Override
	public void exitDate(DateContext ctx) {
		if (ctx.exception != null) return;

		Era era = (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer dayOfMonth = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();
		
		// For the latest date we could either return null, or a copy of the earliest date,
		// since the UI doesn't care. Use a copy of the earliest date, since it makes
		// things easier here if we don't have to test for null up the tree.
		
		stack.push(new Date(year, numMonth, dayOfMonth).withEra(era));
		stack.push(new Date(year, numMonth, dayOfMonth).withEra(era));
	}

	@Override
	public void exitInvStrDate(InvStrDateContext ctx) {
		if (ctx.exception != null) return;

		// Reorder the arguments.
		
		Integer dayOfMonth = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();
		Integer year = (Integer) stack.pop();
		Era era = (Era) stack.pop();
		
		stack.push(numMonth);
		stack.push(dayOfMonth);
		stack.push(year);
		stack.push(era);
	}

	@Override
	public void exitMonth(MonthContext ctx) {
		if (ctx.exception != null) return;

		Era era = (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();
		
		stack.push(new Date(year, numMonth, 1).withEra(era));
		stack.push(new Date(year, numMonth, DateUtils.getDaysInMonth(numMonth, year)).withEra(era));		
	}
	
	@Override
	public void exitInvMonthYear(InvMonthYearContext ctx) {
		if (ctx.exception != null) return;
		
		// Invert the arguments.

		Integer numMonth = (Integer) stack.pop();
		Integer year = (Integer) stack.pop();
		Era era = (Era) stack.pop();

		stack.push(numMonth);
		stack.push(year);
		stack.push(era);
	}

	@Override
	public void exitQuarterYear(QuarterYearContext ctx) {
		if (ctx.exception != null) return;
		
		Era era = (Era) stack.pop();		
		Integer year = (Integer) stack.pop();
		Integer quarter = (Integer) stack.pop();

		stack.push(DateUtils.getQuarterYearStartDate(year, quarter).withEra(era));
		stack.push(DateUtils.getQuarterYearEndDate(year, quarter).withEra(era));
	}

	@Override
	public void exitHalfYear(HalfYearContext ctx) {
		if (ctx.exception != null) return;
		
		Era era = (Era) stack.pop();		
		Integer year = (Integer) stack.pop();
		Integer half = (Integer) stack.pop();

		stack.push(DateUtils.getHalfYearStartDate(year, half).withEra(era));
		stack.push(DateUtils.getHalfYearEndDate(year, half).withEra(era));
	}	
	
	@Override
	public void exitInvSeasonYear(InvSeasonYearContext ctx) {
		if (ctx.exception != null) return;
		
		// Invert the arguments.
		
		Integer quarter = (Integer) stack.pop();
		Integer year = (Integer) stack.pop();
		Era era = (Era) stack.pop();
		
		stack.push(quarter);
		stack.push(year);
		stack.push(era);
	}

	@Override
	public void exitYear(YearContext ctx) {
		if (ctx.exception != null) return;

		Era era = (Era) stack.pop();
		Integer year = (Integer) stack.pop();
		
		stack.push(new Date(year, FIRST_MONTH, FIRST_DAY_OF_FIRST_MONTH).withEra(era));
		stack.push(new Date(year, LAST_MONTH, LAST_DAY_OF_LAST_MONTH).withEra(era));
	}

	@Override
	public void exitNumYear(NumYearContext ctx) {
		if (ctx.exception != null) return;

		// Convert the string to a number,
		// and push on the stack.

		stack.push(new Integer(ctx.NUMBER().getText()));
	}

	@Override
	public void exitNumMonth(NumMonthContext ctx) {
		if (ctx.exception != null) return;

		// Convert the string to a number,
		// and push on the stack.
		
		stack.push(new Integer(ctx.NUMBER().getText()));
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
			n = new Integer(ctx.NTHSTR().getText().replaceAll("[^\\d]+$", ""));
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
		
		if (monthStr.equals("sept")) {
			monthStr = "sep";
		}

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
	public void exitEra(EraContext ctx) {
		if (ctx.exception != null) return;

		// Convert the string to an Era,
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

		stack.push(new Integer(ctx.NUMBER().getText()));
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