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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.collectionspace.services.jaxb.adapter.DateAdapter;
import org.jspecify.annotations.NonNull;


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

    @Id
    @Column(name = "csid", nullable = false, length = 128)
    @XmlAttribute(name = "csid")
    private String csid;

    @Basic
    @Column(name = "screen_name", nullable = false, length = 128)
    @XmlElement(required = true)
    private String screenName;

    @Basic
    @Column(name = "person_ref_name")
    private String personRefName;

    @Basic
    @Column(name = "email", nullable = false)
    @XmlElement(required = true)
    private String email;

    @Basic
    @Column(name = "phone")
    @XmlElement(required = true)
    private String phone;

    @Basic
    @Column(name = "mobile")
    @XmlElement(required = true)
    private String mobile;

    @Basic
    @Column(name = "userid", nullable = false, length = 128)
    @XmlElement(required = true)
    private String userId;

    private byte[] password;

    @Basic
    @Column(name = "require_sso")
    private Boolean requireSSO;

    @OneToMany(targetEntity = AccountTenant.class, cascade = {
        CascadeType.ALL
    })
    @JoinColumn(name = "TENANTS_ACCOUNTS_COMMON_CSID")
    @XmlElement(required = true)
    private List<AccountTenant> tenants;

    @Basic
    @Column(name = "status", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    @XmlElement(required = true)
    private Status status;

    @Basic
    @Column(name = "metadata_protection")
    private String metadataProtection;

    @Basic
    @Column(name = "roles_protection")
    private String rolesProtection;

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

    @Transient
    private RoleList roleList;

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getPersonRefName() {
        return personRefName;
    }

    public void setPersonRefName(String personRefName) {
        this.personRefName = personRefName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public Boolean isRequireSSO() {
        return requireSSO;
    }

    public void setRequireSSO(Boolean requireSSO) {
        this.requireSSO = requireSSO;
    }

    @NonNull
    public List<AccountTenant> getTenants() {
        if (tenants == null) {
            tenants = new ArrayList<>();
        }
        return this.tenants;
    }

    public void setTenants(List<AccountTenant> tenants) {
        this.tenants = tenants;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMetadataProtection() {
        return metadataProtection;
    }

    public void setMetadataProtection(String metadataProtection) {
        this.metadataProtection = metadataProtection;
    }

    public String getRolesProtection() {
        return rolesProtection;
    }

    public void setRolesProtection(String rolesProtection) {
        this.rolesProtection = rolesProtection;
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

    public RoleList getRoleList() {
        return roleList;
    }

    public void setRoleList(RoleList roleList) {
        this.roleList = roleList;
    }

    public String getCsid() {
        return csid;
    }

    public void setCsid(String csid) {
        this.csid = csid;
    }

    public void setCreatedAtItem(Date createdAt) {
        setCreatedAt(createdAt);
    }

    public void setUpdatedAtItem(Date updatedAt) {
        setUpdatedAt(updatedAt);
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
