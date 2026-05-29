package org.collectionspace.services.authorization.perms;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlEnum
@XmlType(name = "effect_type")
public enum EffectType {
    PERMIT,
    DENY;

    public String value() {
        return name();
    }

    public static EffectType fromValue(String v) {
        return valueOf(v);
    }
}
