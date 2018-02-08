package org.collectionspace.services.structureddate;

public interface StructuredDateEvaluator {
	public StructuredDateInternal evaluate(String displayDate) throws StructuredDateFormatException;
}
