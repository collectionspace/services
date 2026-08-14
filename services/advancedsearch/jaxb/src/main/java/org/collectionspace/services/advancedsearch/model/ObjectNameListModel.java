package org.collectionspace.services.advancedsearch.model;

import java.util.List;

import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.ObjectNameGroup;

public class ObjectNameListModel {
	private ObjectNameListModel() {}

	/**
	 * @param collectionObject The CollectionObject Common part
	 * @return The value of the objectNameControlled field
	 */
	public static String objectNameControlled(final CollectionobjectsCommon collectionObject) {
		String objectNameControlled = null;

		if (collectionObject != null && collectionObject.getObjectNameList() != null) {
			final List<ObjectNameGroup> objectNameGroups = collectionObject.getObjectNameList().getObjectNameGroup();
			if (!objectNameGroups.isEmpty()) {
				final ObjectNameGroup objectNameGroup = objectNameGroups.getFirst();
				objectNameControlled = objectNameGroup.getObjectNameControlled();
			}
		}

		return objectNameControlled;
	}

	/**
	 * @param collectionObject The CollectionObject Common part
	 * @return The value of the objectName field
	 */
	public static String objectName(final CollectionobjectsCommon collectionObject) {
		String objectName = null;
		if (collectionObject!= null && collectionObject.getObjectNameList() != null) {
			final List<ObjectNameGroup> objectNameGroups = collectionObject.getObjectNameList().getObjectNameGroup();
			if (!objectNameGroups.isEmpty()) {
				final ObjectNameGroup objectNameGroup = objectNameGroups.getFirst();
				objectName = objectNameGroup.getObjectName();
			}
		}

		return objectName;
	}
}
