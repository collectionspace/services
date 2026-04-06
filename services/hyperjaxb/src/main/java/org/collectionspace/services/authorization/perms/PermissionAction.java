package org.collectionspace.services.authorization.perms;

import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "permission_action")
@Entity(name = "PermissionAction")
@Table(name = "permissions_actions")
@Inheritance(strategy = InheritanceType.JOINED)
public class PermissionAction {

    @XmlElement(required = true)
    protected ActionType name;

    @XmlElement(required = true)
    protected String objectIdentity;

    @XmlElement(required = true)
    protected String objectIdentityResource;

    @XmlAttribute(name = "Hjid")
    protected Long hjid;

    @Basic
    @Column(name = "name", nullable = false, length = 128)
    @Enumerated(EnumType.STRING)
    public ActionType getName() {
        return name;
    }

    public PermissionAction setName(ActionType name) {
        this.name = name;
        return this;
    }

    @Basic
    @Column(name = "objectIdentity", nullable = false, length = 128)
    public String getObjectIdentity() {
        return objectIdentity;
    }

    public PermissionAction setObjectIdentity(String objectIdentity) {
        this.objectIdentity = objectIdentity;
        return this;
    }

    @Basic
    @Column(name = "objectIdentityResource", nullable = false, length = 128)
    public String getObjectIdentityResource() {
        return objectIdentityResource;
    }

    public PermissionAction setObjectIdentityResource(String objectIdentityResource) {
        this.objectIdentityResource = objectIdentityResource;
        return this;
    }

    @Id
    @Column(name = "HJID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getHjid() {
        return hjid;
    }

    public PermissionAction setHjid(Long hjid) {
        this.hjid = hjid;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PermissionAction that = (PermissionAction) o;
        return name == that.name
               && Objects.equals(objectIdentity,that.objectIdentity)
               && Objects.equals(objectIdentityResource, that.objectIdentityResource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, objectIdentity, objectIdentityResource);
    }
}
