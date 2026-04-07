package org.collectionspace.services.authorization.perms;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "permission_action")
@Entity(name = "PermissionAction")
@Table(name = "permissions_actions")
@Inheritance(strategy = InheritanceType.JOINED)
public class PermissionAction {

    @Basic
    @Column(name = "name", nullable = false, length = 128)
    @Enumerated(EnumType.STRING)
    @XmlElement(required = true)
    private ActionType name;

    @Basic
    @Column(name = "objectIdentity", nullable = false, length = 128)
    @XmlElement(required = true)
    private String objectIdentity;

    @Basic
    @Column(name = "objectIdentityResource", nullable = false, length = 128)
    @XmlElement(required = true)
    private String objectIdentityResource;

    @Id
    @Column(name = "HJID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @XmlAttribute(name = "Hjid")
    private Long hjid;

    public ActionType getName() {
        return name;
    }

    public PermissionAction setName(ActionType name) {
        this.name = name;
        return this;
    }

    public String getObjectIdentity() {
        return objectIdentity;
    }

    public PermissionAction setObjectIdentity(String objectIdentity) {
        this.objectIdentity = objectIdentity;
        return this;
    }

    public String getObjectIdentityResource() {
        return objectIdentityResource;
    }

    public PermissionAction setObjectIdentityResource(String objectIdentityResource) {
        this.objectIdentityResource = objectIdentityResource;
        return this;
    }

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
                && Objects.equals(objectIdentity, that.objectIdentity)
                && Objects.equals(objectIdentityResource, that.objectIdentityResource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, objectIdentity, objectIdentityResource);
    }
}
