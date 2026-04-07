package org.collectionspace.services.authorization;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.jspecify.annotations.NonNull;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "account_permission")
public class AccountPermission {

    @XmlElement(required = true)
    protected List<AccountValue> account;

    @XmlElement(required = true)
    protected List<PermissionValue> permission;

    @NonNull
    public List<AccountValue> getAccount() {
        if (account == null) {
            account = new ArrayList<>();
        }
        return account;
    }

    public AccountPermission setAccount(List<AccountValue> account) {
        this.account = account;
        return this;
    }

    @NonNull
    public List<PermissionValue> getPermission() {
        if (permission == null) {
            permission = new ArrayList<>();
        }
        return permission;
    }

    public AccountPermission setPermission(List<PermissionValue> permission) {
        this.permission = permission;
        return this;
    }
}
