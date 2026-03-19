package org.collectionspace.services.authorization.perms;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "permissions_list")
public class PermissionsList {

    @XmlSchemaType(name = "unsignedInt")
    protected long pageNum;

    @XmlSchemaType(name = "unsignedInt")
    protected long pageSize;

    @XmlSchemaType(name = "unsignedInt")
    protected long itemsInPage;

    @XmlSchemaType(name = "unsignedInt")
    protected long totalItems;

    @XmlElement(required = true)
    protected List<Permission> permission;

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

    public List<Permission> getPermission() {
        return permission;
    }

    public PermissionsList setPermission(
        List<Permission> permission) {
        this.permission = permission;
        return this;
    }
}
