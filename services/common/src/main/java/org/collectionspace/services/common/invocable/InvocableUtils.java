package org.collectionspace.services.common.invocable;

import org.collectionspace.services.jaxb.InvocableJAXBSchema;

public class InvocableUtils {

	/**
	 * Returns the standard property name for an invocable schema, given
	 * and invocation mode string.
	 * @param schema If not null, the returned property name will be qualified with
	 * 					this schema name.
	 * @param invocationMode one of Invocable.INVOCATION_MODE_*
	 * @return
	 */
	public static String getPropertyNameForInvocationMode(String schema, String invocationMode) {
		String modeProperty = null;
		if(Invocable.INVOCATION_MODE_SINGLE.equalsIgnoreCase(invocationMode)) {
			modeProperty = InvocableJAXBSchema.SUPPORTS_SINGLE_DOC;
		} else if(Invocable.INVOCATION_MODE_LIST.equalsIgnoreCase(invocationMode)) {
			modeProperty = InvocableJAXBSchema.SUPPORTS_DOC_LIST;
		} else if(Invocable.INVOCATION_MODE_GROUP.equalsIgnoreCase(invocationMode)) {
			modeProperty = InvocableJAXBSchema.SUPPORTS_GROUP;
		} else if(Invocable.INVOCATION_MODE_NO_CONTEXT.equalsIgnoreCase(invocationMode)) {
			modeProperty = InvocableJAXBSchema.SUPPORTS_NO_CONTEXT;
		} else {
			throw new IllegalArgumentException("QueryManagerNuxeoImpl: unknown Invocation Mode: "
	    			+invocationMode);
		}
		return (schema!=null)? (schema+":"+modeProperty):modeProperty;
	}

}
