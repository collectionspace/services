package org.collectionspace.services.account;

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
import javax.persistence.Transient;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "account_tenant")
@Entity(name = "AccountTenant")
@Table(name = "accounts_tenants")
@Inheritance(strategy = InheritanceType.JOINED)
public class AccountTenant {

    @XmlElement(name = "tenant_id", required = true)
    private String tenantId;

    @XmlAttribute(name = "Hjid")
    private Long hjid;

    @Basic
    @Column(name = "tenant_id", nullable = false, length = 128)
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String value) {
        this.tenantId = value;
    }

    @Transient
    public boolean isSetTenantId() {
        return (this.tenantId!= null);
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountTenant that = (AccountTenant) o;
        return Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tenantId);
    }
}
