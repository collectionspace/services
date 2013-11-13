package org.collectionspace.services.structureddate;

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
			"displayDate: " + getDisplayDate() + "\n" +
			"note:        " + getNote() + "\n" +
			"association: " + getAssociation() + "\n" +
			"period:      " + getPeriod() + "\n";
		
		if (getEarliestSingleDate() != null) {
			string += 
				"\n" +
				"earliestSingleDate: \n" +
				getEarliestSingleDate().toString() + "\n";
		}
		
		if (getLatestDate() != null) {
			string += 
				"\n" +
				"latestDate: \n" +
				getLatestDate().toString() + "\n";			
		}
		
		return string;
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
