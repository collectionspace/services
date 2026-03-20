package org.collectionspace.services.authorization;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "x_account-roles-list")
public class XAccountRolesList {

    @XmlElement(name = "x_account-role-list-item")
    protected List<XAccountRoleListItem> xAccountRoleListItem;

    public List<XAccountRoleListItem> getXAccountRoleListItem() {
        if (xAccountRoleListItem == null) {
            xAccountRoleListItem = new ArrayList<>();
        }
        return this.xAccountRoleListItem;
    }

    public void setXAccountRoleListItem(List<XAccountRoleListItem> xAccountRoleListItem) {
        this.xAccountRoleListItem = xAccountRoleListItem;
    }

    public boolean isSetXAccountRoleListItem() {
        return ((this.xAccountRoleListItem!= null)&&(!this.xAccountRoleListItem.isEmpty()));
    }

    public void unsetXAccountRoleListItem() {
        this.xAccountRoleListItem = null;
    }

}
