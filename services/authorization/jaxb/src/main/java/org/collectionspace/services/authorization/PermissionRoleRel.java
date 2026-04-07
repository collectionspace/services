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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.datatype.XMLGregorianCalendar;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import org.jvnet.hyperjaxb3.xml.bind.annotation.adapters.XMLGregorianCalendarAsDateTime;
import org.jvnet.hyperjaxb3.xml.bind.annotation.adapters.XmlAdapterUtils;

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

    @XmlElement(required = true)
    protected String permissionId;

    protected String permissionResource;

    protected String actionGroup;

    @XmlElement(required = true)
    protected String roleId;

    protected String roleName;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar createdAt;

    @XmlAttribute(name = "Hjid")
    protected Long hjid;

    @Basic
    @Column(name = "permission_id", nullable = false, length = 128)
    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String value) {
        this.permissionId = value;
    }

    @Transient
    public boolean isSetPermissionId() {
        return (this.permissionId!= null);
    }

    @Basic
    @Column(name = "permission_resource", nullable = true, length = 255)
    public String getPermissionResource() {
        return permissionResource;
    }

    public void setPermissionResource(String value) {
        this.permissionResource = value;
    }

    @Transient
    public boolean isSetPermissionResource() {
        return (this.permissionResource!= null);
    }

    @Basic
    @Column(name = "actionGroup", nullable = true, length = 255)
    public String getActionGroup() {
        return actionGroup;
    }

    public void setActionGroup(String value) {
        this.actionGroup = value;
    }

    @Transient
    public boolean isSetActionGroup() {
        return (this.actionGroup!= null);
    }

    @Basic
    @Column(name = "role_id", nullable = false, length = 128)
    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String value) {
        this.roleId = value;
    }

    @Transient
    public boolean isSetRoleId() {
        return (this.roleId!= null);
    }

    @Basic
    @Column(name = "role_name", nullable = true, length = 255)
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String value) {
        this.roleName = value;
    }

    @Transient
    public boolean isSetRoleName() {
        return (this.roleName!= null);
    }

    @Transient
    public XMLGregorianCalendar getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(XMLGregorianCalendar value) {
        this.createdAt = value;
    }

    @Transient
    public boolean isSetCreatedAt() {
        return (this.createdAt!= null);
    }

    @Id
    @Column(name = "HJID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getHjid() {
        return hjid;
    }

    public void setHjid(Long value) {
        this.hjid = value;
    }

    @Basic
    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreatedAtItem() {
        return XmlAdapterUtils.unmarshall(XMLGregorianCalendarAsDateTime.class, this.getCreatedAt());
    }

    public void setCreatedAtItem(Date target) {
        setCreatedAt(XmlAdapterUtils.marshall(XMLGregorianCalendarAsDateTime.class, target));
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
