package org.collectionspace.services.authorization;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import org.jspecify.annotations.NonNull;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "roles_list")
public class RolesList {

    @XmlSchemaType(name = "unsignedInt")
    private long pageNum;

    @XmlSchemaType(name = "unsignedInt")
    private long pageSize;

    @XmlSchemaType(name = "unsignedInt")
    private long itemsInPage;

    @XmlSchemaType(name = "unsignedInt")
    private long totalItems;

    @XmlElement(required = true)
    private List<Role> role;

    public long getPageNum() {
        return pageNum;
    }

    public RolesList setPageNum(long pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    public long getPageSize() {
        return pageSize;
    }

    public RolesList setPageSize(long pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public long getItemsInPage() {
        return itemsInPage;
    }

    public RolesList setItemsInPage(long itemsInPage) {
        this.itemsInPage = itemsInPage;
        return this;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public RolesList setTotalItems(long totalItems) {
        this.totalItems = totalItems;
        return this;
    }

    @NonNull
    public List<Role> getRole() {
        if (role == null) {
            role = new ArrayList<>();
        }
        return role;
    }

    public RolesList setRole(List<Role> role) {
        this.role = role;
        return this;
    }
}
