package org.collectionspace.services.authorization;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.jspecify.annotations.NonNull;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "account_role")
public class AccountRole {

    private SubjectType subject;

    @XmlElement(required = true)
    private List<AccountValue> account;

    @XmlElement(required = true)
    private List<RoleValue> role;

    public SubjectType getSubject() {
        return subject;
    }

    public AccountRole setSubject(SubjectType subject) {
        this.subject = subject;
        return this;
    }

    @NonNull
    public List<AccountValue> getAccount() {
        if (account == null) {
            account = new ArrayList<>();
        }
        return account;
    }

    public AccountRole setAccount(List<AccountValue> account) {
        this.account = account;
        return this;
    }

    @NonNull
    public List<RoleValue> getRole() {
        if (role == null) {
            role = new ArrayList<>();
        }
        return role;
    }

    public AccountRole setRole(List<RoleValue> role) {
        this.role = role;
        return this;
    }
}
