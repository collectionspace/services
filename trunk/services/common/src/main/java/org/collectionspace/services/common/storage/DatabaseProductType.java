/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common.storage;

/**
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class DatabaseProductType {

    private final String name;
    private final String propertiesFileName;
    private static final String PROPERTIES_FILE_SUFFIX = ".properties";

    private DatabaseProductType(String name) {
        this.name = name;
        propertiesFileName = name + PROPERTIES_FILE_SUFFIX;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getPropertiesFileName() {
        return propertiesFileName;
    }

    public static final DatabaseProductType MYSQL = new DatabaseProductType("mysql");
    public static final DatabaseProductType POSTGRESQL = new DatabaseProductType("postgresql");

    public static final DatabaseProductType UNRECOGNIZED = new DatabaseProductType("unrecognized");
}
