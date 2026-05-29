package org.collectionspace.services.account;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "role_value")
public class RoleValue {

    private String roleRelationshipId;

    @XmlElement(required = true)
    private String roleId;

    @XmlElement(required = true)
    private String roleName;

    @XmlElement(required = true)
    private String displayName;

    @XmlElement(required = true)
    private String tenantId;

    public String getRoleRelationshipId() {
        return roleRelationshipId;
    }

    public void setRoleRelationshipId(String roleRelationshipId) {
        this.roleRelationshipId = roleRelationshipId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

}
