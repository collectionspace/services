package org.collectionspace.services.authorization;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "account_permission")
public class AccountPermission {

    @XmlElement(required = true)
    protected List<AccountValue> account;

    @XmlElement(required = true)
    protected List<PermissionValue> permission;

    public List<AccountValue> getAccount() {
        return account;
    }

    public AccountPermission setAccount(List<AccountValue> account) {
        this.account = account;
        return this;
    }

    public List<PermissionValue> getPermission() {
        return permission;
    }

    public AccountPermission setPermission(
        List<PermissionValue> permission) {
        this.permission = permission;
        return this;
    }
}
