package org.collectionspace.services.advancedsearch.model;

import java.util.ArrayList;
import java.util.List;

import org.collectionspace.services.collectionobject.ContentConceptList;

public class ContentConceptListModel {

	public static String contentConceptListDisplayString(ContentConceptList conceptList) {
		List<String> displayConcepts = new ArrayList<String>();
		if(null != conceptList) {
			List<String> concepts = conceptList.getContentConcept();
			for (String conceptRefname : concepts) {
				displayConcepts.add(displayNameFromRefName(conceptRefname));
			}
		}

		return String.join(",", displayConcepts);
	}

	private static String displayNameFromRefName(String refname) {
		// e.g.
		// urn:cspace:core.collectionspace.org:conceptauthorities:name(concept):item:name(FooConcept1749234493809)'FooConcept'
		// -> FooConcept
		// TODO: there is probably code somewhere for doing this
	    String displayName = refname;
	    if(refname.indexOf("'") < refname.lastIndexOf("'")) {
	    	displayName = refname.substring(refname.indexOf("'")+1, refname.lastIndexOf("'"));
	    }
	    
		return displayName;
	}

}
