package org.collectionspace.services.authentication;

import java.math.BigInteger;
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
@XmlRootElement(name = "token")
@Entity(name = "Token")
@Table(name = "tokens")
@Inheritance(strategy = InheritanceType.JOINED)
public class Token {

    @Id
    @Column(name = "id", nullable = false, length = 128)
    @XmlElement(required = true)
    protected String id;

    @Basic
    @Column(name = "account_csid", nullable = false, length = 128)
    @XmlElement(required = true)
    protected String accountCsid;

    @Basic
    @Column(name = "tenant_id", nullable = false, length = 128)
    @XmlElement(required = true)
    protected String tenantId;

    @Basic
    @Column(name = "expire_seconds", nullable = false, precision = 20, scale = 0)
    @XmlElement(required = true)
    protected BigInteger expireSeconds;

    @Basic
    @Column(name = "enabled", nullable = false)
    protected boolean enabled;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    @XmlJavaTypeAdapter(DateAdapter.class)
    protected Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    @XmlJavaTypeAdapter(DateAdapter.class)
    protected Date updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountCsid() {
        return accountCsid;
    }

    public void setAccountCsid(String accountCsid) {
        this.accountCsid = accountCsid;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public BigInteger getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(BigInteger expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
        Token token = (Token) o;
        return enabled == token.enabled
               && Objects.equals(id, token.id)
               && Objects.equals(accountCsid, token.accountCsid)
               && Objects.equals(tenantId, token.tenantId)
               && Objects.equals(expireSeconds, token.expireSeconds)
               && Objects.equals(createdAt, token.createdAt)
               && Objects.equals(updatedAt, token.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountCsid, tenantId, expireSeconds, enabled, createdAt, updatedAt);
    }
}
