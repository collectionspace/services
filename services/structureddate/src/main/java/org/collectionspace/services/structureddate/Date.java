package org.collectionspace.services.structureddate;

public class Date {
	public static final Integer DEFAULT_YEAR = null;
	public static final Integer DEFAULT_MONTH = null;
	public static final Integer DEFAULT_DAY = null;
	public static final Era DEFAULT_ERA = Era.CE;
	public static final Certainty DEFAULT_CERTAINTY = null;
	public static final Qualifier DEFAULT_QUALIFIER = null;
	public static final Integer DEFAULT_QUALIFIER_VALUE = null;
	public static final QualifierUnit DEFAULT_QUALIFIER_UNIT = null;

	private Integer year;
	private Integer month;
	private Integer day;
	private Era era;
	private Certainty certainty;
	private Qualifier qualifier;
	private Integer qualifierValue;
	private QualifierUnit qualifierUnit;
	private String scalarValue;

	public Date() {
		this(DEFAULT_YEAR, DEFAULT_MONTH, DEFAULT_DAY, DEFAULT_ERA, DEFAULT_CERTAINTY, DEFAULT_QUALIFIER, DEFAULT_QUALIFIER_VALUE, DEFAULT_QUALIFIER_UNIT);
	}
	
	public Date(Integer year, Integer month, Integer day) {
		this(year, month, day, DEFAULT_ERA, DEFAULT_CERTAINTY, DEFAULT_QUALIFIER, DEFAULT_QUALIFIER_VALUE, DEFAULT_QUALIFIER_UNIT);
	}

	public Date(Integer year, Integer month, Integer day, Era era, Certainty certainty, Qualifier qualifier, Integer qualifierValue, QualifierUnit qualifierUnit) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.era = era;
		this.certainty = certainty;
		this.qualifier = qualifier;
		this.qualifierValue = qualifierValue;
		this.qualifierUnit = qualifierUnit;
	}
	
	public void qualify(Qualifier qualifier, Integer qualifierValue, QualifierUnit qualifierUnit) {
		this.qualifier = qualifier;
		this.qualifierValue = qualifierValue;
		this.qualifierUnit = qualifierUnit;			
	}
	
	public String toString() {
		return
			"year:           " + getYear() + "\n" +
			"month:          " + getMonth() + "\n" + 
			"day:            " + getDay() + "\n" +
			"era:            " + getEra() + "\n" +
			"certainty:      " + getCertainty() + "\n" +
			"qualifier:      " + getQualifier() + "\n" +
			"qualifierValue: " + getQualifierValue() + "\n" +
			"qualifierUnit:  " + getQualifierUnit() + "\n" +
			"scalarValue:    " + getScalarValue() + "\n";
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	public Era getEra() {
		return era;
	}

	public void setEra(Era era) {
		this.era = era;
	}

	public Certainty getCertainty() {
		return certainty;
	}

	public void setCertainty(Certainty certainty) {
		this.certainty = certainty;
	}

	public Qualifier getQualifier() {
		return qualifier;
	}

	public void setQualifier(Qualifier qualifier) {
		this.qualifier = qualifier;
	}

	public Integer getQualifierValue() {
		return qualifierValue;
	}

	public void setQualifierValue(Integer qualifierValue) {
		this.qualifierValue = qualifierValue;
	}

	public QualifierUnit getQualifierUnit() {
		return qualifierUnit;
	}

	public void setQualifierUnit(QualifierUnit qualifierUnit) {
		this.qualifierUnit = qualifierUnit;
	}

	public String getScalarValue() {
		return scalarValue;
	}

	public void setScalarValue(String scalarValue) {
		this.scalarValue = scalarValue;
	}
}

