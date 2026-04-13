package org.collectionspace.services.authorization;

import java.util.Date;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.collectionspace.services.jaxb.adapter.DateAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "permission_role_rel")
@Entity(name = "PermissionRoleRel")
@Table(name = "permissions_roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {
        "permission_id",
        "role_id"
    })
})
@Inheritance(strategy = InheritanceType.JOINED)
public class PermissionRoleRel {

    @Id
    @Column(name = "HJID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @XmlAttribute(name = "Hjid")
    private Long hjid;

    @Basic
    @Column(name = "permission_id", nullable = false, length = 128)
    @XmlElement(required = true)
    private String permissionId;

    @Basic
    @Column(name = "permission_resource")
    private String permissionResource;

    @Basic
    @Column(name = "actionGroup")
    private String actionGroup;

    @Basic
    @Column(name = "role_id", nullable = false, length = 128)
    @XmlElement(required = true)
    private String roleId;

    @Basic
    @Column(name = "role_name")
    private String roleName;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date createdAt;

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    public String getPermissionResource() {
        return permissionResource;
    }

    public void setPermissionResource(String permissionResource) {
        this.permissionResource = permissionResource;
    }

    public String getActionGroup() {
        return actionGroup;
    }

    public void setActionGroup(String actionGroup) {
        this.actionGroup = actionGroup;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Long getHjid() {
        return hjid;
    }

    public void setHjid(Long hjid) {
        this.hjid = hjid;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PermissionRoleRel that = (PermissionRoleRel) o;
        return Objects.equals(permissionId, that.permissionId)
               && Objects.equals(permissionResource, that.permissionResource)
               && Objects.equals(actionGroup, that.actionGroup)
               && Objects.equals(roleId, that.roleId)
               && Objects.equals(roleName, that.roleName)
               && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permissionId, permissionResource, actionGroup, roleId, roleName, createdAt);
    }
}
