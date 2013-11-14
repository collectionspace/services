package org.collectionspace.services.structureddate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.collectionspace.services.structureddate.antlr.ANTLRStructuredDateEvaluator;


/**
 * A CollectionSpace structured date. 
 */
public class StructuredDate {
	private static final StructuredDateEvaluator evaluator = new ANTLRStructuredDateEvaluator();
	
	private String displayDate;
	private String note;
	private String association;
	private String period;
	
	private Date earliestSingleDate;
	private Date latestDate;
	
	private Boolean scalarValuesComputed;
		
	public StructuredDate() {
		
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
		
		StructuredDate that = (StructuredDate) obj;

		return 
			new EqualsBuilder()
				.append(this.getDisplayDate(), that.getDisplayDate())
				.append(this.getAssociation(), that.getAssociation())
				.append(this.getNote(), that.getNote())
				.append(this.getPeriod(), that.getPeriod())
				.append(this.getEarliestSingleDate(), that.getEarliestSingleDate())
				.append(this.areScalarValuesComputed(), that.areScalarValuesComputed())
				.isEquals();
	}
	
	public void computeScalarValues() {
		// TODO: Implement this, if necessary.
	}
	
	public static StructuredDate parse(String displayDate) throws StructuredDateFormatException {
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

	public Boolean areScalarValuesComputed() {
		return scalarValuesComputed;
	}

	public void setScalarValuesComputed(Boolean scalarValuesComputed) {
		this.scalarValuesComputed = scalarValuesComputed;
	}
}
