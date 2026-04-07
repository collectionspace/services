package org.collectionspace.services.authentication;

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
@XmlRootElement(name = "user")
@Entity(name = "User")
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {

    @Id
    @Column(name = "username", nullable = false, length = 128)
    @XmlElement(required = true)
    protected String username;

    @Basic
    @Column(name = "passwd", nullable = false, length = 128)
    @XmlElement(required = true)
    protected String passwd;

    @Basic
    @Column(name = "salt", nullable = false, length = 128)
    @XmlElement(required = true)
    protected String salt;

    @Transient
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar createdAt;

    @Transient
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar updatedAt;

    @Transient
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastLogin;

    public String getUsername() {
        return username;
    }

    public void setUsername(String value) {
        this.username = value;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String value) {
        this.passwd = value;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String value) {
        this.salt = value;
    }

    public XMLGregorianCalendar getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(XMLGregorianCalendar value) {
        this.createdAt = value;
    }

    public XMLGregorianCalendar getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(XMLGregorianCalendar value) {
        this.updatedAt = value;
    }

    public XMLGregorianCalendar getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(XMLGregorianCalendar value) {
        this.lastLogin = value;
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

    @Basic
    @Column(name = "lastLogin")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastLoginItem() {
        return XmlAdapterUtils.unmarshall(XMLGregorianCalendarAsDateTime.class, this.getLastLogin());
    }

    public void setLastLoginItem(Date target) {
        setLastLogin(XmlAdapterUtils.marshall(XMLGregorianCalendarAsDateTime.class, target));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(username, user.username)
               && Objects.equals(passwd, user.passwd)
               && Objects.equals(salt, user.salt)
               && Objects.equals(createdAt, user.createdAt)
               && Objects.equals(updatedAt, user.updatedAt)
               && Objects.equals(lastLogin, user.lastLogin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, passwd, salt, createdAt, updatedAt, lastLogin);
    }
}
