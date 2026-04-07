package org.collectionspace.services.authorization;


import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.jspecify.annotations.NonNull;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "permissions_roles_list")
public class PermissionsRolesList {

    @XmlElement(required = true)
    protected List<PermissionRole> permissionRole;

    @NonNull
    public List<PermissionRole> getPermissionRole() {
        if (permissionRole == null) {
            permissionRole = new ArrayList<>();
        }
        return this.permissionRole;
    }

    public void setPermissionRole(List<PermissionRole> permissionRole) {
        this.permissionRole = permissionRole;
    }

}
