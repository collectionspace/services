package org.collectionspace.services.authorization;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class XAccountRoleListItem {

    @XmlElement(required = true)
    protected String csid;

    @XmlElement(required = true)
    protected String roleName;

    @XmlElement(required = true)
    protected String roleId;
    public String getCsid() {
        return csid;
    }

    public void setCsid(String value) {
        this.csid = value;
    }

    public boolean isSetCsid() {
        return (this.csid!= null);
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

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String value) {
        this.roleId = value;
    }

    public boolean isSetRoleId() {
        return (this.roleId!= null);
    }

}
