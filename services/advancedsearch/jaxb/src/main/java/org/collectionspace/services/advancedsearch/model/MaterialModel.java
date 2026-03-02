package org.collectionspace.services.advancedsearch.model;

import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.MaterialGroupList;

public class MaterialModel {

    public static String material(CollectionobjectsCommon collectionObject) {
        String material = null;
        if (collectionObject != null && collectionObject.getMaterialGroupList() != null) {
            MaterialGroupList materialGroup = collectionObject.getMaterialGroupList();
            if (!materialGroup.getMaterialGroup().isEmpty()) {
                material = materialGroup.getMaterialGroup().get(0).getMaterial();
            }
        }
        return material;
    }
}
