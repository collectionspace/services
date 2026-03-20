package org.collectionspace.services.authorization;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "permission_value")
public class PermissionValue {

    protected String permRelationshipId;

    @XmlElement(required = true)
    protected String permissionId;

    @XmlElement(required = true)
    protected String resourceName;

    @XmlElement(required = true)
    protected String actionGroup;

    @XmlElement(required = true)
    protected String tenantId;

    public String getPermRelationshipId() {
        return permRelationshipId;
    }

    public PermissionValue setPermRelationshipId(String permRelationshipId) {
        this.permRelationshipId = permRelationshipId;
        return this;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public PermissionValue setPermissionId(String permissionId) {
        this.permissionId = permissionId;
        return this;
    }

    public String getResourceName() {
        return resourceName;
    }

    public PermissionValue setResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    public String getActionGroup() {
        return actionGroup;
    }

    public PermissionValue setActionGroup(String actionGroup) {
        this.actionGroup = actionGroup;
        return this;
    }

    public String getTenantId() {
        return tenantId;
    }

    public PermissionValue setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }
}
