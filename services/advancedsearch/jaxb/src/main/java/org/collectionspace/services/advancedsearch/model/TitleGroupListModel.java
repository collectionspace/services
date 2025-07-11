package org.collectionspace.services.advancedsearch.model;

import java.util.List;

import org.collectionspace.services.collectionobject.TitleGroup;
import org.collectionspace.services.collectionobject.TitleGroupList;

public class TitleGroupListModel {
	public static String titleGroupListToDisplayString(TitleGroupList tlList) {
		String returnString = "";
		List<TitleGroup> titleGroups = tlList.getTitleGroup();
		// "Title: Display 1st Title OR 1st Controlled Nomenclature combined with Uncontrolled Nomenclature OR 1st Taxon with Preservation Form" from https://docs.google.com/spreadsheets/d/103jyxa2oCtt8U0IQ25xsOyIxqwKvPNXlcCtcjGlT5tQ/edit?gid=0#gid=0
		// FIXME: we are not fully implementing the above logic below
		if(null != titleGroups) {
			if(!titleGroups.isEmpty()) {
				TitleGroup titleGroup = titleGroups.get(0);
				if(null != titleGroup.getTitle()) {
					returnString = titleGroup.getTitle();
				}
			}
		}

		return returnString;
	}
}
