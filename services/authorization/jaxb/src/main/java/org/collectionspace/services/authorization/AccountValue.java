package org.collectionspace.services.authorization;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "account_value")
public class AccountValue {

    @XmlElement(required = true)
    protected String accountId;

    @XmlElement(required = true)
    protected String screenName;

    @XmlElement(required = true)
    protected String userId;

    @XmlElement(required = true)
    protected String tenantId;

    public String getAccountId() {
        return accountId;
    }

    public AccountValue setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getScreenName() {
        return screenName;
    }

    public AccountValue setScreenName(String screenName) {
        this.screenName = screenName;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public AccountValue setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getTenantId() {
        return tenantId;
    }

    public AccountValue setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }
}
