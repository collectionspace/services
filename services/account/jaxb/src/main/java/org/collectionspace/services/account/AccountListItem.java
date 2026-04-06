package org.collectionspace.services.account;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import org.jspecify.annotations.NonNull;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class AccountListItem {

    @XmlElement(required = true)
    protected String screenName;

    @XmlElement(required = true)
    protected String userid;

    @XmlElement(required = true)
    protected String tenantid;

    @XmlElement(required = true)
    protected List<AccountTenant> tenants;

    @XmlElement(required = true)
    protected String personRefName;

    @XmlElement(required = true)
    protected String email;

    protected Boolean requireSSO;

    @XmlElement(required = true)
    protected Status status;

    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String uri;

    @XmlElement(required = true)
    protected String csid;

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String value) {
        this.screenName = value;
    }

    public boolean isSetScreenName() {
        return (this.screenName!= null);
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String value) {
        this.userid = value;
    }

    public boolean isSetUserid() {
        return (this.userid!= null);
    }

    public String getTenantid() {
        return tenantid;
    }

    public void setTenantid(String value) {
        this.tenantid = value;
    }

    public boolean isSetTenantid() {
        return (this.tenantid!= null);
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

    public boolean isSetTenants() {
        return ((this.tenants!= null)&&(!this.tenants.isEmpty()));
    }

    public void unsetTenants() {
        this.tenants = null;
    }

    public String getPersonRefName() {
        return personRefName;
    }

    public void setPersonRefName(String value) {
        this.personRefName = value;
    }

    public boolean isSetPersonRefName() {
        return (this.personRefName!= null);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
    }

    public boolean isSetEmail() {
        return (this.email!= null);
    }

    public Boolean isRequireSSO() {
        return requireSSO;
    }

    public void setRequireSSO(Boolean value) {
        this.requireSSO = value;
    }

    public boolean isSetRequireSSO() {
        return (this.requireSSO!= null);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status value) {
        this.status = value;
    }

    public boolean isSetStatus() {
        return (this.status!= null);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String value) {
        this.uri = value;
    }

    public boolean isSetUri() {
        return (this.uri!= null);
    }

    public String getCsid() {
        return csid;
    }

    public void setCsid(String value) {
        this.csid = value;
    }

    public boolean isSetCsid() {
        return (this.csid!= null);
    }

}
