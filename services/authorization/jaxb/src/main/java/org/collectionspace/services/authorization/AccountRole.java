package org.collectionspace.services.authorization;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "account_role")
public class AccountRole {

    protected SubjectType subject;

    @XmlElement(required = true)
    protected List<AccountValue> account;

    @XmlElement(required = true)
    protected List<RoleValue> role;

    public SubjectType getSubject() {
        return subject;
    }

    public AccountRole setSubject(SubjectType subject) {
        this.subject = subject;
        return this;
    }

    public List<AccountValue> getAccount() {
        return account;
    }

    public AccountRole setAccount(List<AccountValue> account) {
        this.account = account;
        return this;
    }

    public List<RoleValue> getRole() {
        return role;
    }

    public AccountRole setRole(List<RoleValue> role) {
        this.role = role;
        return this;
    }
}
