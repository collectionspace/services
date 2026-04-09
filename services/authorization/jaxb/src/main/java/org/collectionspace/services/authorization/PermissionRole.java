package org.collectionspace.services.authorization;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.jspecify.annotations.NonNull;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "permission_role")
public class PermissionRole {

    private SubjectType subject;

    @XmlElement(required = true)
    private List<PermissionValue> permission;

    @XmlElement(required = true)
    private List<RoleValue> role;

    public SubjectType getSubject() {
        return subject;
    }

    public PermissionRole setSubject(SubjectType subject) {
        this.subject = subject;
        return this;
    }

    @NonNull
    public List<PermissionValue> getPermission() {
        if (permission == null) {
            permission = new ArrayList<>();
        }
        return permission;
    }

    public PermissionRole setPermission(List<PermissionValue> permission) {
        this.permission = permission;
        return this;
    }

    @NonNull
    public List<RoleValue> getRole() {
        if (role == null) {
            role = new ArrayList<>();
        }
        return role;
    }

    public PermissionRole setRole(List<RoleValue> role) {
        this.role = role;
        return this;
    }
}
