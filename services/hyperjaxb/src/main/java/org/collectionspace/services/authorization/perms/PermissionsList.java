package org.collectionspace.services.authorization.perms;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import org.jspecify.annotations.NonNull;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "permissions_list")
public class PermissionsList {

    @XmlSchemaType(name = "unsignedInt")
    private long pageNum;

    @XmlSchemaType(name = "unsignedInt")
    private long pageSize;

    @XmlSchemaType(name = "unsignedInt")
    private long itemsInPage;

    @XmlSchemaType(name = "unsignedInt")
    private long totalItems;

    @XmlElement(required = true)
    private List<Permission> permission;

    public long getPageNum() {
        return pageNum;
    }

    public PermissionsList setPageNum(long pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    public long getPageSize() {
        return pageSize;
    }

    public PermissionsList setPageSize(long pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public long getItemsInPage() {
        return itemsInPage;
    }

    public PermissionsList setItemsInPage(long itemsInPage) {
        this.itemsInPage = itemsInPage;
        return this;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public PermissionsList setTotalItems(long totalItems) {
        this.totalItems = totalItems;
        return this;
    }

    @NonNull
    public List<Permission> getPermission() {
        if (permission == null) {
            permission = new ArrayList<>();
        }
        return permission;
    }

    public PermissionsList setPermission(List<Permission> permission) {
        this.permission = permission;
        return this;
    }
}
