package org.collectionspace.services.account;

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

    public void setRoleRelationshipId(String value) {
        this.roleRelationshipId = value;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String value) {
        this.roleId = value;
    }

    public boolean isSetRoleId() {
        return (this.roleId!= null);
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String value) {
        this.roleName = value;
    }

    public boolean isSetRoleName() {
        return (this.roleName!= null);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String value) {
        this.displayName = value;
    }

    public boolean isSetDisplayName() {
        return (this.displayName!= null);
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String value) {
        this.tenantId = value;
    }

    public boolean isSetTenantId() {
        return (this.tenantId!= null);
    }

}
