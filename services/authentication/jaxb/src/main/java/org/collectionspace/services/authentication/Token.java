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
import javax.persistence.Transient;
import javax.xml.datatype.XMLGregorianCalendar;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import org.jvnet.hyperjaxb3.xml.bind.annotation.adapters.XMLGregorianCalendarAsDateTime;
import org.jvnet.hyperjaxb3.xml.bind.annotation.adapters.XmlAdapterUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "token")
@Entity(name = "Token")
@Table(name = "tokens")
@Inheritance(strategy = InheritanceType.JOINED)
public class Token {

    @XmlElement(required = true)
    protected String id;

    @XmlElement(required = true)
    protected String accountCsid;

    @XmlElement(required = true)
    protected String tenantId;

    @XmlElement(required = true)
    protected BigInteger expireSeconds;

    protected boolean enabled;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar createdAt;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar updatedAt;

    @Id
    @Column(name = "id", nullable = false, length = 128)
    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    @Basic
    @Column(name = "account_csid", nullable = false, length = 128)
    public String getAccountCsid() {
        return accountCsid;
    }

    public void setAccountCsid(String value) {
        this.accountCsid = value;
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
    @Column(name = "expire_seconds", nullable = false, precision = 20, scale = 0)
    public BigInteger getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(BigInteger value) {
        this.expireSeconds = value;
    }

    @Basic
    @Column(name = "enabled", nullable = false)
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        this.enabled = value;
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
