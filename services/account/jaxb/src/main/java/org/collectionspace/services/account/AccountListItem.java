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
    private String screenName;

    @XmlElement(required = true)
    private String userid;

    @XmlElement(required = true)
    private String tenantid;

    @XmlElement(required = true)
    private List<AccountTenant> tenants;

    @XmlElement(required = true)
    private String personRefName;

    @XmlElement(required = true)
    private String email;

    private Boolean requireSSO;

    @XmlElement(required = true)
    private Status status;

    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    private String uri;

    @XmlElement(required = true)
    private String csid;

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

    public String getTenantid() {
        return tenantid;
    }

    public void setTenantid(String value) {
        this.tenantid = value;
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

    public String getPersonRefName() {
        return personRefName;
    }

    public void setPersonRefName(String value) {
        this.personRefName = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
    }

    public Boolean isRequireSSO() {
        return requireSSO;
    }

    public void setRequireSSO(Boolean value) {
        this.requireSSO = value;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status value) {
        this.status = value;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String value) {
        this.uri = value;
    }

    public String getCsid() {
        return csid;
    }

    public void setCsid(String value) {
        this.csid = value;
    }

}
