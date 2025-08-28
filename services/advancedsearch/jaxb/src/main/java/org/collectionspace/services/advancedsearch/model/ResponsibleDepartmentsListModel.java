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
		// NOTE "Display all values separated by comma", from https://docs.google.com/spreadsheets/d/103jyxa2oCtt8U0IQ25xsOyIxqwKvPNXlcCtcjGlT5tQ/edit?gid=0#gid=0
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
	public static String responsibleDepartmentsListDisplayString(ResponsibleDepartmentsList rdl) {
		String rdlString = "";
		if (null != rdl && null != rdl.getResponsibleDepartment() && rdl.getResponsibleDepartment().size() > 0) {
			ResponsibleDepartment rd = rdl.getResponsibleDepartment().get(0);
			rdlString = rd.getName();
		}
		return rdlString;
	}
}
