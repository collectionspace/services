package org.collectionspace.services.structureddate;

public interface StructuredDateEvaluator {
	public StructuredDate evaluate(String displayDate) throws StructuredDateFormatException;
}
