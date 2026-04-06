package org.collectionspace.services.account;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jspecify.annotations.NonNull;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "accounts-common-list")
public class AccountsCommonList extends AbstractCommonList {

    @XmlElement(name = "account-list-item", required = true)
    protected List<AccountListItem> accountListItem;

    @NonNull
    public List<AccountListItem> getAccountListItem() {
        if (accountListItem == null) {
            accountListItem = new ArrayList<>();
        }
        return this.accountListItem;
    }

    public void setAccountListItem(List<AccountListItem> accountListItem) {
        this.accountListItem = accountListItem;
    }

    public boolean isSetAccountListItem() {
        return ((this.accountListItem!= null)&&(!this.accountListItem.isEmpty()));
    }

}
