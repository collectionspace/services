package org.collectionspace.services.authorization.perms;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.datatype.XMLGregorianCalendar;
import org.jspecify.annotations.NonNull;
import org.jvnet.hyperjaxb3.xml.bind.annotation.adapters.XMLGregorianCalendarAsDateTime;
import org.jvnet.hyperjaxb3.xml.bind.annotation.adapters.XmlAdapterUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "permission")
@Entity(name = "Permission")
@Table(name = "permissions")
@Inheritance(strategy = InheritanceType.JOINED)
public class Permission {

    @Id
    @Column(name = "csid", nullable = false, length = 128)
    @XmlAttribute(name = "csid")
    private String csid;

    @Basic
    @Column(name = "description")
    private String description;

    @Basic
    @Column(name = "resource_name", nullable = false, length = 128)
    @XmlElement(required = true)
    private String resourceName;

    @Basic
    @Column(name = "attribute_name", length = 128)
    private String attributeName;

    @Basic
    @Column(name = "action_group", length = 128)
    private String actionGroup;

    @OneToMany(
            targetEntity = PermissionAction.class,
            cascade = {CascadeType.ALL})
    @JoinColumn(name = "ACTION__PERMISSION_CSID")
    @XmlElement(required = true)
    private List<PermissionAction> action;

    @Basic
    @Column(name = "effect", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    @XmlElement(required = true)
    private EffectType effect;

    @Basic
    @Column(name = "metadata_protection")
    private String metadataProtection;

    @Basic
    @Column(name = "actions_protection")
    private String actionsProtection;

    @Basic
    @Column(name = "tenant_id", nullable = false, length = 128)
    @XmlElement(name = "tenant_id", required = true)
    private String tenantId;

    @Transient
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar createdAt;

    @Transient
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar updatedAt;

    public String getDescription() {
        return description;
    }

    public Permission setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getResourceName() {
        return resourceName;
    }

    public Permission setResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public Permission setAttributeName(String attributeName) {
        this.attributeName = attributeName;
        return this;
    }

    public String getActionGroup() {
        return actionGroup;
    }

    public Permission setActionGroup(String actionGroup) {
        this.actionGroup = actionGroup;
        return this;
    }

    @NonNull
    public List<PermissionAction> getAction() {
        if (action == null) {
            action = new ArrayList<>();
        }
        return action;
    }

    public Permission setAction(List<PermissionAction> action) {
        this.action = action;
        return this;
    }

    public EffectType getEffect() {
        return effect;
    }

    public Permission setEffect(EffectType effect) {
        this.effect = effect;
        return this;
    }

    public String getMetadataProtection() {
        return metadataProtection;
    }

    public Permission setMetadataProtection(String metadataProtection) {
        this.metadataProtection = metadataProtection;
        return this;
    }

    public String getActionsProtection() {
        return actionsProtection;
    }

    public Permission setActionsProtection(String actionsProtection) {
        this.actionsProtection = actionsProtection;
        return this;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Permission setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public XMLGregorianCalendar getCreatedAt() {
        return createdAt;
    }

    public Permission setCreatedAt(XMLGregorianCalendar createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public XMLGregorianCalendar getUpdatedAt() {
        return updatedAt;
    }

    public Permission setUpdatedAt(XMLGregorianCalendar updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public String getCsid() {
        return csid;
    }

    public Permission setCsid(String csid) {
        this.csid = csid;
        return this;
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
        Permission that = (Permission) o;
        return Objects.equals(description, that.description)
                && Objects.equals(resourceName, that.resourceName)
                && Objects.equals(attributeName, that.attributeName)
                && Objects.equals(actionGroup, that.actionGroup)
                && effect == that.effect
                && Objects.equals(metadataProtection, that.metadataProtection)
                && Objects.equals(actionsProtection, that.actionsProtection)
                && Objects.equals(tenantId, that.tenantId)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt)
                && Objects.equals(csid, that.csid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                description,
                resourceName,
                attributeName,
                actionGroup,
                effect,
                metadataProtection,
                actionsProtection,
                tenantId,
                createdAt,
                updatedAt,
                csid);
    }
}
