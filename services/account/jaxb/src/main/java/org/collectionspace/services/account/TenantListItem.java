package org.collectionspace.services.account;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class TenantListItem {

    @XmlElement(required = true)
    private String id;

    @XmlElement(required = true)
    private String name;

    private boolean disabled;

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean value) {
        this.disabled = value;
    }

}
