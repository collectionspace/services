package org.collectionspace.services.authorization;

import java.util.Date;
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
import org.jvnet.hyperjaxb3.xml.bind.annotation.adapters.XMLGregorianCalendarAsDateTime;
import org.jvnet.hyperjaxb3.xml.bind.annotation.adapters.XmlAdapterUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "account_role_rel")
@Entity(name = "AccountRoleRel")
@Table(name = "accounts_roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {
        "account_id",
        "role_id"
    })
})
@Inheritance(strategy = InheritanceType.JOINED)
public class AccountRoleRel {

    private String accountId;

    private String screenName;

    @XmlElement(required = true)
    private String userId;

    @XmlElement(required = true)
    private String roleId;

    private String roleName;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar createdAt;

    @XmlAttribute(name = "Hjid")
    private Long hjid;

    @Basic
    @Column(name = "account_id", nullable = false, length = 128)
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String value) {
        this.accountId = value;
    }

    @Basic
    @Column(name = "screen_name")
    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String value) {
        this.screenName = value;
    }

    @Basic
    @Column(name = "user_id", nullable = false, length = 128)
    public String getUserId() {
        return userId;
    }

    public void setUserId(String value) {
        this.userId = value;
    }

    @Basic
    @Column(name = "role_id", nullable = false, length = 128)
    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String value) {
        this.roleId = value;
    }

    @Basic
    @Column(name = "role_name")
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String value) {
        this.roleName = value;
    }

    @Transient
    public XMLGregorianCalendar getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(XMLGregorianCalendar value) {
        this.createdAt = value;
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

    @Basic
    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreatedAtItem() {
        return XmlAdapterUtils.unmarshall(XMLGregorianCalendarAsDateTime.class, this.getCreatedAt());
    }

    public void setCreatedAtItem(Date target) {
        setCreatedAt(XmlAdapterUtils.marshall(XMLGregorianCalendarAsDateTime.class, target));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountRoleRel that = (AccountRoleRel) o;
        return Objects.equals(accountId, that.accountId)
               && Objects.equals(screenName, that.screenName)
               && Objects.equals(userId, that.userId)
               && Objects.equals(roleId, that.roleId)
               && Objects.equals(roleName, that.roleName)
               && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, screenName, userId, roleId, roleName, createdAt);
    }
}
