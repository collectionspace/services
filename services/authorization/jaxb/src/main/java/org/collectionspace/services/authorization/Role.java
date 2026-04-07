package org.collectionspace.services.authorization;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import org.jspecify.annotations.NonNull;
import org.jvnet.hyperjaxb3.xml.bind.annotation.adapters.XMLGregorianCalendarAsDateTime;
import org.jvnet.hyperjaxb3.xml.bind.annotation.adapters.XmlAdapterUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "role")
@Entity(name = "Role")
@Table(name = "roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {
        "rolename",
        "tenant_id"
    }),
    @UniqueConstraint(columnNames = {
        "displayname",
        "tenant_id"
    })
})
@Inheritance(strategy = InheritanceType.JOINED)
public class Role {

    @XmlElement(required = true)
    protected String displayName;

    @XmlElement(required = true)
    protected String roleName;

    protected String description;

    protected String roleGroup;

    @XmlElement(name = "tenant_id", required = true)
    protected String tenantId;

    protected String metadataProtection;

    protected String permsProtection;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar createdAt;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar updatedAt;

    @XmlElement(required = true)
    protected List<PermissionValue> permission;

    @XmlAttribute(name = "csid")
    protected String csid;

    @Basic
    @Column(name = "displayname", nullable = false, length = 200)
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String value) {
        this.displayName = value;
    }

    @Basic
    @Column(name = "rolename", nullable = false, length = 200)
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String value) {
        this.roleName = value;
    }

    @Basic
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    @Basic
    @Column(name = "rolegroup")
    public String getRoleGroup() {
        return roleGroup;
    }

    public void setRoleGroup(String value) {
        this.roleGroup = value;
    }

    @Basic
    @Column(name = "tenant_id", nullable = false, length = 128)
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String value) {
        this.tenantId = value;
    }

    @Basic
    @Column(name = "metadata_protection")
    public String getMetadataProtection() {
        return metadataProtection;
    }

    public void setMetadataProtection(String value) {
        this.metadataProtection = value;
    }

    @Basic
    @Column(name = "perms_protection")
    public String getPermsProtection() {
        return permsProtection;
    }

    public void setPermsProtection(String value) {
        this.permsProtection = value;
    }

    @Transient
    public XMLGregorianCalendar getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(XMLGregorianCalendar value) {
        this.createdAt = value;
    }

    @Transient
    public XMLGregorianCalendar getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(XMLGregorianCalendar value) {
        this.updatedAt = value;
    }

    @Transient
    @NonNull
    public List<PermissionValue> getPermission() {
        if (permission == null) {
            permission = new ArrayList<>();
        }
        return this.permission;
    }

    public void setPermission(List<PermissionValue> permission) {
        this.permission = permission;
    }

    @Id
    @Column(name = "csid", nullable = false, length = 128)
    public String getCsid() {
        return csid;
    }

    public void setCsid(String value) {
        this.csid = value;
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

    @Basic
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getUpdatedAtItem() {
        return XmlAdapterUtils.unmarshall(XMLGregorianCalendarAsDateTime.class, this.getUpdatedAt());
    }

    public void setUpdatedAtItem(Date target) {
        setUpdatedAt(XmlAdapterUtils.marshall(XMLGregorianCalendarAsDateTime.class, target));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Role role = (Role) o;
        return Objects.equals(displayName, role.displayName)
               && Objects.equals(roleName, role.roleName)
               && Objects.equals(description, role.description)
               && Objects.equals(roleGroup, role.roleGroup)
               && Objects.equals(tenantId, role.tenantId)
               && Objects.equals(metadataProtection, role.metadataProtection)
               && Objects.equals(permsProtection, role.permsProtection)
               && Objects.equals(createdAt, role.createdAt)
               && Objects.equals(updatedAt, role.updatedAt)
               && Objects.equals(permission, role.permission)
               && Objects.equals(csid, role.csid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, roleName, description, roleGroup, tenantId, metadataProtection,
                            permsProtection, createdAt, updatedAt, permission, csid);
    }
}
