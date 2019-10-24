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

		string +=
			"\n" +
			"\tearliestScalarValue: " + getEarliestScalarValue() + "\n" +
			"\tlatestScalarValue: " + getLatestScalarValue() + "\n" +
			"\tscalarValuesComputed: " + areScalarValuesComputed() + "\n";

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
				// .append(this.getEarliestScalarValue(), that.getEarliestScalarValue())
				// .append(this.getLatestScalarValue(), that.getLatestScalarValue())
				.append(this.areScalarValuesComputed(), that.areScalarValuesComputed())
				.isEquals();
	}

	private String computeEarliestScalarValue() {
		Date earliestDate = getEarliestSingleDate();

		Integer year = null;
		Integer month = null;
		Integer day = null;
		Era era = null;

		if (earliestDate != null) {
			year = earliestDate.getYear();
			month = earliestDate.getMonth();
			day = earliestDate.getDay();
			era = earliestDate.getEra();
		}

		if (year == null && month == null && day == null) {
			return null;
		}

		if (year == null) {
			// The date must at least specify a year.
			throw new InvalidDateException("year must not be null");
		}

		if (day != null && month == null) {
			// If a day is specified, the month must be specified.
			throw new InvalidDateException("month may not be null when day is not null");
		}

		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		if (month == null) {
			month = 1;
			day = 1;
		}

		if (day == null) {
			day = 1;
		}

		Date date = new Date(year, month, day, era);

		return DateUtils.formatEarliestScalarValue(date);
	}

	private String computeLatestScalarValue() {
		Date latestDate = getLatestDate();

		Integer year = null;
		Integer month = null;
		Integer day = null;
		Era era = null;

		if (latestDate != null) {
			year = latestDate.getYear();
			month = latestDate.getMonth();
			day = latestDate.getDay();
			era = latestDate.getEra();
		}

		if (year == null && month == null && day == null) {
			// No latest date parts are specified. Inherit year, month, and day from earliest/single.

			Date earliestDate = getEarliestSingleDate();

			// TODO: What if no date parts are specified, but the era/certainty/qualifier is different than
			// the earliest/single?

			if (earliestDate != null) {
				year = earliestDate.getYear();
				month = earliestDate.getMonth();
				day = earliestDate.getDay();
			}
		}

		if (year == null && month == null && day == null) {
			return null;
		}

		if (year == null) {
			// The date must at least specify a year.
			throw new InvalidDateException("year must not be null");
		}

		if (day != null && month == null) {
			// If a day is specified, the month must be specified.
			throw new InvalidDateException("month may not be null when day is not null");
		}

		if (era == null) {
			era = Date.DEFAULT_ERA;
		}

		if (month == null) {
			month = 12;
			month = 31;
		}

		if (day == null) {
			day = DateUtils.getDaysInMonth(month, year, era);
		}

		Date date = new Date(year, month, day, era);

		// Add one day to the latest day, since that's what the UI has historically (*sigh*) done.
		DateUtils.addDays(date, 1);

		return DateUtils.formatLatestScalarValue(date);
	}

	public void computeScalarValues() {
		setEarliestScalarValue(computeEarliestScalarValue());
		setLatestScalarValue(computeLatestScalarValue());
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
