package org.collectionspace.services.authorization;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlEnum
@XmlType(name = "subject_type")
public enum SubjectType {

    ACCOUNT, PERMISSION, ROLE;

    public String value() {
        return name();
    }

    public static SubjectType fromValue(String v) {
        return valueOf(v);
    }
}
