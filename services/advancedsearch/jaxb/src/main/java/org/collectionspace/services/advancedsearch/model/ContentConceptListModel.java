package org.collectionspace.services.advancedsearch.model;

import java.util.List;

import org.collectionspace.services.collectionobject.ContentConceptList;

public class ContentConceptListModel {

	public static String contentConceptListDisplayString(ContentConceptList ccl) {
		String cclString = "";
		List<String> ccls = ccl.getContentConcept();
		if(null != ccls) {
			cclString = String.join(",", ccls);
		}
		return cclString;
	}

}
