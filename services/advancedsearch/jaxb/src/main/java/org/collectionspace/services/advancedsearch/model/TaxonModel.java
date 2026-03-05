package org.collectionspace.services.advancedsearch.model;

import java.util.List;

import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.domain.naturalhistory_extension.CollectionobjectsNaturalhistory;
import org.collectionspace.services.collectionobject.domain.naturalhistory_extension.DeterminationHistoryGroup;
import org.collectionspace.services.collectionobject.domain.naturalhistory_extension.DeterminationHistoryGroupList;
import org.collectionspace.services.collectionobject.domain.naturalhistory_extension.TaxonomicIdentGroup;
import org.collectionspace.services.collectionobject.domain.naturalhistory_extension.TaxonomicIdentGroupList;

public class TaxonModel {

	public static String taxon(final CollectionobjectsNaturalhistory naturalHistory) {
		String taxon = null;
		if (naturalHistory != null && naturalHistory.getTaxonomicIdentGroupList() != null) {
			TaxonomicIdentGroupList taxonomicIdentGroupList = naturalHistory.getTaxonomicIdentGroupList();
			List<TaxonomicIdentGroup> taxonomicIdentGroups = taxonomicIdentGroupList.getTaxonomicIdentGroup();
			if (!taxonomicIdentGroups.isEmpty()) {
				TaxonomicIdentGroup taxonGroup = taxonomicIdentGroups.get(0);
				taxon = taxonGroup.getTaxon();
			}
		}

		return taxon;
	}

	public static String preservationForm(final CollectionobjectsCommon common) {
		String form = null;
		if (common != null && common.getForms() != null) {
			List<String> forms = common.getForms().getForm();
			if (!forms.isEmpty()) {
				form = forms.get(0);
			}
		}

		return form;
	}

	public static String determinationTaxon(final CollectionobjectsNaturalhistory naturalHistory) {
		String taxon = null;
		if (naturalHistory != null && naturalHistory.getTaxonomicIdentGroupList() != null) {
			DeterminationHistoryGroupList determinationGroupList = naturalHistory.getDeterminationHistoryGroupList();
			List<DeterminationHistoryGroup> determinationGroups = determinationGroupList.getDeterminationHistoryGroup();
			if (!determinationGroups.isEmpty()) {
				DeterminationHistoryGroup group = determinationGroups.get(0);
				taxon = group.getDeterminationTaxon();
			}
		}

		return taxon;
	}
}
