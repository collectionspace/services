package org.collectionspace.services.account;

import java.util.Date;
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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.collectionspace.services.jaxb.adapter.DateAdapter;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "tenant")
@Entity(name = "Tenant")
@Table(name = "tenants")
@Inheritance(strategy = InheritanceType.JOINED)
public class Tenant {

    @Id
    @Column(name = "id", nullable = false, length = 128)
    @XmlElement(required = true)
    private String id;

    @Basic
    @Column(name = "name", nullable = false)
    @XmlElement(required = true)
    private String name;

    @Basic
    @Column(name = "config_md5hash")
    @XmlElement(required = true)
    private String configMD5Hash;

    @Basic
    @Column(name = "authorities_initialized", nullable = false)
    private boolean authoritiesInitialized;

    @Basic
    @Column(name = "disabled", nullable = false)
    private boolean disabled;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfigMD5Hash() {
        return configMD5Hash;
    }

    public void setConfigMD5Hash(String configMD5Hash) {
        this.configMD5Hash = configMD5Hash;
    }

    public boolean isAuthoritiesInitialized() {
        return authoritiesInitialized;
    }

    public void setAuthoritiesInitialized(boolean authoritiesInitialized) {
        this.authoritiesInitialized = authoritiesInitialized;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tenant tenant = (Tenant) o;
        return authoritiesInitialized == tenant.authoritiesInitialized
               && disabled == tenant.disabled
               && Objects.equals(id, tenant.id)
               && Objects.equals(name, tenant.name)
               && Objects.equals(configMD5Hash, tenant.configMD5Hash)
               && Objects.equals(createdAt, tenant.createdAt)
               && Objects.equals(updatedAt, tenant.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, configMD5Hash, authoritiesInitialized, disabled, createdAt, updatedAt);
    }
}
