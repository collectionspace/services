package org.collectionspace.services.advancedsearch.model;

import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.ResponsibleDepartmentList;

public class ResponsibleDepartmentsListModel {

	public static String responsibleDepartmentString(CollectionobjectsCommon collectionObject) {
		String responsibleDepartment = null;
		if (collectionObject != null && collectionObject.getResponsibleDepartments() != null) {
			ResponsibleDepartmentList responsibleDepartments = collectionObject.getResponsibleDepartments();
			if (!responsibleDepartments.getResponsibleDepartment().isEmpty()) {
				responsibleDepartment = responsibleDepartments.getResponsibleDepartment().get(0);
			}
		}
		return responsibleDepartment;
	}

}
