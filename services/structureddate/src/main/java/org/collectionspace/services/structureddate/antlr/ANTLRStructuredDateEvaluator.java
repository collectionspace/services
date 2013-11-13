package org.collectionspace.services.structureddate.antlr;

import java.util.Stack;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.collectionspace.services.structureddate.Date;
import org.collectionspace.services.structureddate.DateUtils;
import org.collectionspace.services.structureddate.Era;
import org.collectionspace.services.structureddate.QualifierType;
import org.collectionspace.services.structureddate.QualifierUnit;
import org.collectionspace.services.structureddate.StructuredDate;
import org.collectionspace.services.structureddate.StructuredDateEvaluator;
import org.collectionspace.services.structureddate.StructuredDateFormatException;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.CircaYearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.DateRangeOnlyContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.DayOfMonthContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.InvStrDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.MonthOnlyRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NumDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NumDateRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.NumMonthContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.SingleDateOnlyContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.StrDateContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.StrDateRangeContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.StrMonthContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.YearContext;
import org.collectionspace.services.structureddate.antlr.StructuredDateParser.YearOnlyContext;

/**
 * A StructuredDateEvaluator that uses an ANTLR parser to parse the display date,
 * and an ANTLR listener to generate a structured date from the resulting parse
 * tree.
 */
public class ANTLRStructuredDateEvaluator extends StructuredDateBaseListener implements StructuredDateEvaluator {	
	protected StructuredDate result;
	protected Stack<Object> stack;
	
	public ANTLRStructuredDateEvaluator() {

	}
	
	@Override
	public StructuredDate evaluate(String displayDate) throws StructuredDateFormatException {
		stack = new Stack<Object>();

		result = new StructuredDate();
		result.setDisplayDate(displayDate);

		ANTLRInputStream inputStream = new ANTLRInputStream(displayDate);
		StructuredDateLexer lexer = new StructuredDateLexer(inputStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		StructuredDateParser parser = new StructuredDateParser(tokenStream);
		ErrorListener errorListener = new ErrorListener();
		
		parser.addErrorListener(errorListener);
		parser.addParseListener(this);
		parser.displayDate();
		
		return result;
	}

	@Override
	public void exitCircaYear(CircaYearContext ctx) {
		Integer year = (Integer) stack.pop();
		Era era = ctx.BCE() != null ? Era.BCE : Era.CE;
		
		result.setEarliestSingleDate(
			new Date(year, 1, 1)
				.withEra(era)
				.withQualifier(QualifierType.MINUS, 1, QualifierUnit.DAYS)
		);
		
		result.setLatestDate(
			new Date(year, 12, 31)
				.withEra(era)
				.withQualifier(QualifierType.PLUS, 1, QualifierUnit.DAYS)
		);
	}
	
	@Override
	public void exitYearOnly(YearOnlyContext ctx) {
		Integer year = (Integer) stack.pop();
		
		Date date = new Date(year, null, null);
		date.setEra(ctx.BCE() != null ? Era.BCE : Era.CE);
		
		result.setEarliestSingleDate(date);
	}

	@Override
	public void exitSingleDateOnly(SingleDateOnlyContext ctx) {
		result.setEarliestSingleDate((Date) stack.pop());
	}
	
	@Override
	public void exitDateRangeOnly(DateRangeOnlyContext ctx) {
		result.setLatestDate((Date) stack.pop());
		result.setEarliestSingleDate((Date) stack.pop());
	}

	@Override
	public void exitMonthOnlyRange(MonthOnlyRangeContext ctx) {
		Integer year = (Integer) stack.pop();
		Integer numMonthEnd = (Integer) stack.pop();
		Integer numMonthStart = (Integer) stack.pop();
		
		stack.push(new Date(year, numMonthStart, 1));
		stack.push(new Date(year, numMonthEnd, DateUtils.getDaysInMonth(numMonthEnd, year)));		
	}
	
	@Override
	public void exitNumDateRange(NumDateRangeContext ctx) {
		Integer year = (Integer) stack.pop();
		Integer dayOfMonthEnd = (Integer) stack.pop();
		Integer dayOfMonthStart = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();
		
		stack.push(new Date(year, numMonth, dayOfMonthStart));
		stack.push(new Date(year, numMonth, dayOfMonthEnd));		
	}

	@Override
	public void exitStrDateRange(StrDateRangeContext ctx) {
		Integer year = (Integer) stack.pop();
		Integer dayOfMonthEnd = (Integer) stack.pop();
		Integer dayOfMonthStart = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();
		
		stack.push(new Date(year, numMonth, dayOfMonthStart));
		stack.push(new Date(year, numMonth, dayOfMonthEnd));		
	}
	
	@Override
	public void exitStrDate(StrDateContext ctx) {
		Integer year = (Integer) stack.pop();
		Integer dayOfMonth = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();
		
		stack.push(new Date(year, numMonth, dayOfMonth));
	}

	@Override
	public void exitInvStrDate(InvStrDateContext ctx) {
		Integer dayOfMonth = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();
		Integer year = (Integer) stack.pop();
		
		stack.push(new Date(year, numMonth, dayOfMonth));
	}
	
	@Override
	public void exitNumDate(NumDateContext ctx) {
		Integer year = (Integer) stack.pop();
		Integer dayOfMonth = (Integer) stack.pop();
		Integer numMonth = (Integer) stack.pop();
		
		stack.push(new Date(year, numMonth, dayOfMonth));
	}

	@Override
	public void exitYear(YearContext ctx) {
		stack.push(new Integer(ctx.NUMBER().getText()));
	}

	@Override
	public void exitNumMonth(NumMonthContext ctx) {
		stack.push(new Integer(ctx.NUMBER().getText()));
	}
	
	@Override
	public void exitStrMonth(StrMonthContext ctx) {
		String monthStr = ctx.MONTH().getText().toLowerCase();
		
		if (monthStr.endsWith(".")) {
			monthStr = monthStr.substring(0, monthStr.length() - 1);
		}
		
		if (monthStr.equals("sept")) {
			monthStr = "sep";
		}
		
		stack.push(DateUtils.getMonthByName(monthStr));		
	}

	@Override
	public void exitDayOfMonth(DayOfMonthContext ctx) {
		stack.push(new Integer(ctx.NUMBER().getText()));
	}
	
	public static void main(String[] args) {
		StructuredDateEvaluator evaluator = new ANTLRStructuredDateEvaluator();
		
		for (String displayDate : args) {
			try {
				StructuredDate structuredDate = evaluator.evaluate(displayDate);
				System.out.println(structuredDate.toString());
			} catch (StructuredDateFormatException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class ErrorListener extends BaseErrorListener {
		@Override
		public void syntaxError(Recognizer<?,?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
			throw(new StructuredDateFormatException(msg, e));
		}
	}
}