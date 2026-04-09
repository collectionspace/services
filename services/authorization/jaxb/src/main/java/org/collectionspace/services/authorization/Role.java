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

    @Id
    @Column(name = "csid", nullable = false, length = 128)
    @XmlAttribute(name = "csid")
    private String csid;

    @Basic
    @Column(name = "displayname", nullable = false, length = 200)
    @XmlElement(required = true)
    private String displayName;

    @Basic
    @Column(name = "rolename", nullable = false, length = 200)
    @XmlElement(required = true)
    private String roleName;

    @Basic
    @Column(name = "description")
    private String description;

    @Basic
    @Column(name = "rolegroup")
    private String roleGroup;

    @Basic
    @Column(name = "tenant_id", nullable = false, length = 128)
    @XmlElement(name = "tenant_id", required = true)
    private String tenantId;

    @Basic
    @Column(name = "metadata_protection")
    private String metadataProtection;

    @Basic
    @Column(name = "perms_protection")
    private String permsProtection;

    @Transient
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar createdAt;

    @Transient
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar updatedAt;

    @Transient
    @XmlElement(required = true)
    private List<PermissionValue> permission;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoleGroup() {
        return roleGroup;
    }

    public void setRoleGroup(String roleGroup) {
        this.roleGroup = roleGroup;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getMetadataProtection() {
        return metadataProtection;
    }

    public void setMetadataProtection(String metadataProtection) {
        this.metadataProtection = metadataProtection;
    }

    public String getPermsProtection() {
        return permsProtection;
    }

    public void setPermsProtection(String permsProtection) {
        this.permsProtection = permsProtection;
    }

    public XMLGregorianCalendar getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(XMLGregorianCalendar createdAt) {
        this.createdAt = createdAt;
    }

    public XMLGregorianCalendar getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(XMLGregorianCalendar updatedAt) {
        this.updatedAt = updatedAt;
    }

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

    public String getCsid() {
        return csid;
    }

    public void setCsid(String csid) {
        this.csid = csid;
    }

    @Basic
    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreatedAtItem() {
        return XmlAdapterUtils.unmarshall(XMLGregorianCalendarAsDateTime.class, this.getCreatedAt());
    }

    public void setCreatedAtItem(Date createdAt) {
        setCreatedAt(XmlAdapterUtils.marshall(XMLGregorianCalendarAsDateTime.class, createdAt));
    }

    @Basic
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getUpdatedAtItem() {
        return XmlAdapterUtils.unmarshall(XMLGregorianCalendarAsDateTime.class, this.getUpdatedAt());
    }

    public void setUpdatedAtItem(Date updatedAt) {
        setUpdatedAt(XmlAdapterUtils.marshall(XMLGregorianCalendarAsDateTime.class, updatedAt));
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
