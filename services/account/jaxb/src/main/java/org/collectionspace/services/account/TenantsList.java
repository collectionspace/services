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
@XmlRootElement(name = "tenants-list")
public class TenantsList extends AbstractCommonList {

    @XmlElement(name = "tenant-list-item", required = true)
    private List<TenantListItem> tenantListItem;

    @NonNull
    public List<TenantListItem> getTenantListItem() {
        if (tenantListItem == null) {
            tenantListItem = new ArrayList<>();
        }
        return this.tenantListItem;
    }

    public void setTenantListItem(List<TenantListItem> tenantListItem) {
        this.tenantListItem = tenantListItem;
    }

    public boolean isSetTenantListItem() {
        return ((this.tenantListItem!= null)&&(!this.tenantListItem.isEmpty()));
    }

}
