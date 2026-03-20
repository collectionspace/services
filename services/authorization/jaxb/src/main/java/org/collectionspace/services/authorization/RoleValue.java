package org.collectionspace.services.authorization;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "role_value")
public class RoleValue {

    protected String roleRelationshipId;

    @XmlElement(required = true)
    protected String roleId;

    @XmlElement(required = true)
    protected String roleName;

    @XmlElement(required = true)
    protected String displayName;

    @XmlElement(required = true)
    protected String tenantId;

    public String getRoleRelationshipId() {
        return roleRelationshipId;
    }

    public RoleValue setRoleRelationshipId(String roleRelationshipId) {
        this.roleRelationshipId = roleRelationshipId;
        return this;
    }

    public String getRoleId() {
        return roleId;
    }

    public RoleValue setRoleId(String roleId) {
        this.roleId = roleId;
        return this;
    }

    public String getRoleName() {
        return roleName;
    }

    public RoleValue setRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public RoleValue setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getTenantId() {
        return tenantId;
    }

    public RoleValue setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }
}
