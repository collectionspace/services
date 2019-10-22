package org.collectionspace.services.structureddate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.collectionspace.services.structureddate.antlr.ANTLRStructuredDateEvaluator;


/**
 * A CollectionSpace structured date.
 */
public class StructuredDateInternal {
	public static final boolean DEFAULT_SCALAR_VALUES_COMPUTED = false;

	private String displayDate;
	private String note;
	private String association;
	private String period;

	private Date earliestSingleDate;
	private Date latestDate;

	private String earliestScalarValue;
	private String latestScalarValue;
	private Boolean scalarValuesComputed;

	public StructuredDateInternal() {
		scalarValuesComputed = DEFAULT_SCALAR_VALUES_COMPUTED;
	}

	public String toString() {
		String string =
			"\n" +
			"\tdisplayDate: " + getDisplayDate() + "\n" +
			"\tnote:        " + getNote() + "\n" +
			"\tassociation: " + getAssociation() + "\n" +
			"\tperiod:      " + getPeriod() + "\n";

		if (getEarliestSingleDate() != null) {
			string +=
				"\n" +
				"\tearliestSingleDate: \n" +
				getEarliestSingleDate().toString() + "\n";
		}

		if (getLatestDate() != null) {
			string +=
				"\n" +
				"\tlatestDate: \n" +
				getLatestDate().toString() + "\n";
		}

		return string;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj.getClass() != getClass()) {
			return false;
		}

		StructuredDateInternal that = (StructuredDateInternal) obj;

		return
			new EqualsBuilder()
				.append(this.getDisplayDate(), that.getDisplayDate())
				.append(this.getAssociation(), that.getAssociation())
				.append(this.getNote(), that.getNote())
				.append(this.getPeriod(), that.getPeriod())
				.append(this.getEarliestSingleDate(), that.getEarliestSingleDate())
				.append(this.getLatestDate(), that.getLatestDate())
				.append(this.areScalarValuesComputed(), that.areScalarValuesComputed())
				.isEquals();
	}

	public void computeScalarValues() {
		Date earliestDate = getEarliestSingleDate();
		Date latestDate = getLatestDate();

		if (earliestDate == null && latestDate == null) {
			setEarliestScalarValue(null);
			setLatestScalarValue(null);

			return;
		}

		if (earliestDate == null) {
			earliestDate = latestDate.copy();
		}
		else {
			earliestDate = earliestDate.copy();
		}

		if (latestDate == null) {
			latestDate = earliestDate.copy();
		}
		else {
			latestDate = latestDate.copy();
		}

		if (earliestDate.getYear() == null || latestDate.getYear() == null) {
			// The dates must at least specify a year.
			throw new InvalidDateException("year must not be null");
		}

		if (earliestDate.getDay() != null && earliestDate.getMonth() == null) {
			// If a day is specified, the month must be specified.
			throw new InvalidDateException("month may not be null when day is not null");
		}

		if (latestDate.getDay() != null && latestDate.getMonth() == null) {
			// If a day is specified, the month must be specified.
			throw new InvalidDateException("month may not be null when day is not null");
		}

		if (earliestDate.getEra() == null) {
			earliestDate.setEra(Date.DEFAULT_ERA);
		}

		if (latestDate.getEra() == null) {
			latestDate.setEra(Date.DEFAULT_ERA);
		}

		if (earliestDate.getMonth() == null) {
			earliestDate.setMonth(1);
			earliestDate.setDay(1);
		}

		if (latestDate.getMonth() == null) {
			latestDate.setMonth(12);
			latestDate.setDay(31);
		}

		if (earliestDate.getDay() == null) {
			earliestDate.setDay(1);
		}

		if (latestDate.getDay() == null) {
			latestDate.setDay(DateUtils.getDaysInMonth(latestDate.getMonth(), latestDate.getYear(), latestDate.getEra()));
		}

		// Add one day to the latest day, since that's what the UI does.
		DateUtils.addDays(latestDate, 1);

		setEarliestScalarValue(DateUtils.getEarliestScalarValue(earliestDate));
		setLatestScalarValue(DateUtils.getLatestScalarValue(latestDate));
		setScalarValuesComputed(true);
	}

	public static StructuredDateInternal parse(String displayDate) throws StructuredDateFormatException {
		StructuredDateEvaluator evaluator = new ANTLRStructuredDateEvaluator();

		return evaluator.evaluate(displayDate);
	}

	public String getDisplayDate() {
		return displayDate;
	}

	public void setDisplayDate(String displayDate) {
		this.displayDate = displayDate;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getAssociation() {
		return association;
	}

	public void setAssociation(String association) {
		this.association = association;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public Date getEarliestSingleDate() {
		return earliestSingleDate;
	}

	public void setEarliestSingleDate(Date earliestSingleDate) {
		this.earliestSingleDate = earliestSingleDate;
	}

	public Date getLatestDate() {
		return latestDate;
	}

	public void setLatestDate(Date latestDate) {
		this.latestDate = latestDate;
	}

	public boolean isRange() {
		return (getLatestDate() != null);
	}

	public String getEarliestScalarValue() {
		return earliestScalarValue;
	}

	public void setEarliestScalarValue(String earliestScalarValue) {
		this.earliestScalarValue = earliestScalarValue;
	}

	public Boolean areScalarValuesComputed() {
		return scalarValuesComputed;
	}

	public String getLatestScalarValue() {
		return latestScalarValue;
	}

	public void setLatestScalarValue(String latestScalarValue) {
		this.latestScalarValue = latestScalarValue;
	}

	public void setScalarValuesComputed(Boolean scalarValuesComputed) {
		this.scalarValuesComputed = scalarValuesComputed;
	}
}
