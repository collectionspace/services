package org.collectionspace.services.authorization.perms;

import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    private static final QName _Permission_QNAME = new QName("http://collectionspace.org/services/authorization/perms", "permission");

    public Permission createPermission() {
        return new Permission();
    }

    @XmlElementDecl(namespace = "http://collectionspace.org/services/authorization/perms", name = "permission")
    public JAXBElement<Permission> createPermission(Permission value) {
        return new JAXBElement<>(_Permission_QNAME, Permission.class, null, value);
    }

}
