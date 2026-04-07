package org.collectionspace.services.account;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.jspecify.annotations.NonNull;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "roleList")
public class RoleList {

    @XmlElement(required = true)
    private List<RoleValue> role;

    @NonNull
    public List<RoleValue> getRole() {
        if (role == null) {
            role = new ArrayList<>();
        }
        return this.role;
    }

    public void setRole(List<RoleValue> role) {
        this.role = role;
    }

}
