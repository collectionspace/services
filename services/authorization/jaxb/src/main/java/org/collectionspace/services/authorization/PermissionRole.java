package org.collectionspace.services.authorization;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "permission_role")
public class PermissionRole {

    protected SubjectType subject;

    @XmlElement(required = true)
    protected List<PermissionValue> permission;

    @XmlElement(required = true)
    protected List<RoleValue> role;

    public SubjectType getSubject() {
        return subject;
    }

    public PermissionRole setSubject(SubjectType subject) {
        this.subject = subject;
        return this;
    }

    public List<PermissionValue> getPermission() {
        return permission;
    }

    public PermissionRole setPermission(List<PermissionValue> permission) {
        this.permission = permission;
        return this;
    }

    public List<RoleValue> getRole() {
        return role;
    }

    public PermissionRole setRole(List<RoleValue> role) {
        this.role = role;
        return this;
    }
}
