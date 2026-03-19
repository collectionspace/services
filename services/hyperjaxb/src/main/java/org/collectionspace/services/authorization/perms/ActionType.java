package org.collectionspace.services.authorization.perms;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlEnum
@XmlType(name = "action_type")
public enum ActionType {

    CREATE,
    READ,
    UPDATE,
    DELETE,
    SEARCH,
    START,
    STOP,
    RUN,
    ADMIN;

    public String value() {
        return name();
    }

    public static ActionType fromValue(String v) {
        return valueOf(v);
    }
}
