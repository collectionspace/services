package org.collectionspace.services.account;

import java.util.ArrayList;
import java.util.Arrays;
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
import javax.persistence.UniqueConstraint;
import javax.xml.datatype.XMLGregorianCalendar;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import org.jspecify.annotations.NonNull;
import org.jvnet.hyperjaxb3.xml.bind.annotation.adapters.XMLGregorianCalendarAsDateTime;
import org.jvnet.hyperjaxb3.xml.bind.annotation.adapters.XmlAdapterUtils;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "accounts_common")
@Entity(name = "AccountsCommon")
@Table(name = "accounts_common", uniqueConstraints = {
    @UniqueConstraint(columnNames = {
        "userid"
    })
})
@Inheritance(strategy = InheritanceType.JOINED)
public class AccountsCommon {

    @XmlElement(required = true)
    protected String screenName;

    protected String personRefName;

    @XmlElement(required = true)
    protected String email;

    @XmlElement(required = true)
    protected String phone;

    @XmlElement(required = true)
    protected String mobile;

    @XmlElement(required = true)
    protected String userId;

    protected byte[] password;

    protected Boolean requireSSO;

    @XmlElement(required = true)
    protected List<AccountTenant> tenants;

    @XmlElement(required = true)
    protected Status status;

    protected String metadataProtection;

    protected String rolesProtection;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar createdAt;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar updatedAt;

    protected RoleList roleList;

    @XmlAttribute(name = "csid")
    protected String csid;

    @Basic
    @Column(name = "screen_name", nullable = false, length = 128)
    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String value) {
        this.screenName = value;
    }

    @Transient
    public boolean isSetScreenName() {
        return (this.screenName!= null);
    }

    @Basic
    @Column(name = "person_ref_name", length = 255)
    public String getPersonRefName() {
        return personRefName;
    }

    public void setPersonRefName(String value) {
        this.personRefName = value;
    }

    @Transient
    public boolean isSetPersonRefName() {
        return (this.personRefName!= null);
    }

    @Basic
    @Column(name = "email", nullable = false, length = 255)
    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
    }

    @Transient
    public boolean isSetEmail() {
        return (this.email!= null);
    }

    @Basic
    @Column(name = "phone", nullable = true, length = 255)
    public String getPhone() {
        return phone;
    }

    public void setPhone(String value) {
        this.phone = value;
    }

    @Transient
    public boolean isSetPhone() {
        return (this.phone!= null);
    }

    @Basic
    @Column(name = "mobile", nullable = true, length = 255)
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String value) {
        this.mobile = value;
    }

    @Transient
    public boolean isSetMobile() {
        return (this.mobile!= null);
    }

    @Basic
    @Column(name = "userid", nullable = false, length = 128)
    public String getUserId() {
        return userId;
    }

    public void setUserId(String value) {
        this.userId = value;
    }

    @Transient
    public boolean isSetUserId() {
        return (this.userId!= null);
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] value) {
        this.password = value;
    }

    @Transient
    public boolean isSetPassword() {
        return (this.password!= null);
    }

    @Basic
    @Column(name = "require_sso")
    public Boolean isRequireSSO() {
        return requireSSO;
    }

    public void setRequireSSO(Boolean value) {
        this.requireSSO = value;
    }

    @Transient
    public boolean isSetRequireSSO() {
        return (this.requireSSO!= null);
    }

    @NonNull
    @OneToMany(targetEntity = AccountTenant.class, cascade = {
        CascadeType.ALL
    })
    @JoinColumn(name = "TENANTS_ACCOUNTS_COMMON_CSID")
    public List<AccountTenant> getTenants() {
        if (tenants == null) {
            tenants = new ArrayList<>();
        }
        return this.tenants;
    }

    public void setTenants(List<AccountTenant> tenants) {
        this.tenants = tenants;
    }

    @Transient
    public boolean isSetTenants() {
        return ((this.tenants!= null)&&(!this.tenants.isEmpty()));
    }

    public void unsetTenants() {
        this.tenants = null;
    }

    @Basic
    @Column(name = "status", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status value) {
        this.status = value;
    }

    @Transient
    public boolean isSetStatus() {
        return (this.status!= null);
    }

    @Basic
    @Column(name = "metadata_protection", nullable = true, length = 255)
    public String getMetadataProtection() {
        return metadataProtection;
    }

    public void setMetadataProtection(String value) {
        this.metadataProtection = value;
    }

    @Transient
    public boolean isSetMetadataProtection() {
        return (this.metadataProtection!= null);
    }

    @Basic
    @Column(name = "roles_protection", nullable = true, length = 255)
    public String getRolesProtection() {
        return rolesProtection;
    }

    public void setRolesProtection(String value) {
        this.rolesProtection = value;
    }

    @Transient
    public boolean isSetRolesProtection() {
        return (this.rolesProtection!= null);
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

    @Transient
    public XMLGregorianCalendar getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(XMLGregorianCalendar value) {
        this.updatedAt = value;
    }

    @Transient
    public boolean isSetUpdatedAt() {
        return (this.updatedAt!= null);
    }

    @Transient
    public RoleList getRoleList() {
        return roleList;
    }

    public void setRoleList(RoleList value) {
        this.roleList = value;
    }

    @Transient
    public boolean isSetRoleList() {
        return (this.roleList!= null);
    }

    @Id
    @Column(name = "csid", nullable = false, length = 128)
    public String getCsid() {
        return csid;
    }

    public void setCsid(String value) {
        this.csid = value;
    }

    @Transient
    public boolean isSetCsid() {
        return (this.csid!= null);
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
        AccountsCommon that = (AccountsCommon) o;
        return Objects.equals(screenName, that.screenName)
               && Objects.equals(personRefName, that.personRefName)
               && Objects.equals(email, that.email)
               && Objects.equals(phone, that.phone)
               && Objects.equals(mobile, that.mobile)
               && Objects.equals(userId, that.userId)
               && Objects.deepEquals(password, that.password)
               && Objects.equals(requireSSO, that.requireSSO)
               && Objects.equals(tenants, that.tenants)
               && status == that.status
               && Objects.equals(metadataProtection, that.metadataProtection)
               && Objects.equals(rolesProtection, that.rolesProtection)
               && Objects.equals(createdAt, that.createdAt)
               && Objects.equals(updatedAt, that.updatedAt)
               && Objects.equals(roleList, that.roleList)
               && Objects.equals(csid, that.csid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(screenName, personRefName, email, phone, mobile, userId, Arrays.hashCode(password),
                            requireSSO, tenants, status, metadataProtection, rolesProtection, createdAt, updatedAt,
                            roleList, csid);
    }
}
