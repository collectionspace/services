package org.collectionspace.services.advancedsearch.model;

import java.util.List;

import org.collectionspace.services.advancedsearch.ObjectFactory;
import org.collectionspace.services.advancedsearch.ResponsibleDepartment;
import org.collectionspace.services.advancedsearch.ResponsibleDepartmentsList;
import org.collectionspace.services.collectionobject.ResponsibleDepartmentList;

public class ResponsibleDepartmentsListModel {
	private static ObjectFactory objectFactory = new ObjectFactory();
	public static ResponsibleDepartmentsList responsibleDepartmentListToResponsibleDepartmentsList(ResponsibleDepartmentList rdList) {
		ResponsibleDepartmentsList responsibleDepartmentList = objectFactory.createResponsibleDepartmentsList();
		// FIXME: where are the other fields?
		List<String> responsibleDepartmentNames = rdList.getResponsibleDepartment();
		if(null != responsibleDepartmentNames) {
			for(String responsibleDepartmentName : responsibleDepartmentNames) {
				ResponsibleDepartment responsibleDepartment = objectFactory.createResponsibleDepartment();
				responsibleDepartment.setName(responsibleDepartmentName);
				responsibleDepartmentList.getResponsibleDepartment().add(responsibleDepartment);
			}			
		}

		return responsibleDepartmentList;
	}
}
