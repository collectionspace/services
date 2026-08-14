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

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userId) {
        this.userid = userId;
    }

    public String getTenantid() {
        return tenantid;
    }

    public void setTenantid(String tenantId) {
        this.tenantid = tenantId;
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

    public void setPersonRefName(String personRefName) {
        this.personRefName = personRefName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean isRequireSSO() {
        return requireSSO;
    }

    public void setRequireSSO(Boolean requireSSO) {
        this.requireSSO = requireSSO;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCsid() {
        return csid;
    }

    public void setCsid(String csid) {
        this.csid = csid;
    }

}
