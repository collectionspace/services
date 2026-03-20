package org.collectionspace.services.authorization;

import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    private static final String AUTHORIZATION_NS = "http://collectionspace.org/services/authorization";

    private static final QName _AccountRole_QNAME = new QName(AUTHORIZATION_NS, "account_role");
    private static final QName _AccountRoleRel_QNAME = new QName(AUTHORIZATION_NS, "account_role_rel");
    private static final QName _AccountPermission_QNAME = new QName(AUTHORIZATION_NS, "account_permission");
    private static final QName _PermissionRoleRel_QNAME = new QName(AUTHORIZATION_NS, "permission_role_rel");
    private static final QName _Role_QNAME = new QName(AUTHORIZATION_NS, "role");
    private static final QName _RolesList_QNAME = new QName(AUTHORIZATION_NS, "roles_list");
    private static final QName _PermissionRole_QNAME = new QName(AUTHORIZATION_NS, "permission_role");

    @XmlElementDecl(namespace = AUTHORIZATION_NS, name = "account_role")
    public JAXBElement<AccountRole> createAccountRole(AccountRole value) {
        return new JAXBElement<>(_AccountRole_QNAME, AccountRole.class, null, value);
    }

    @XmlElementDecl(namespace = AUTHORIZATION_NS, name = "account_role_rel")
    public JAXBElement<AccountRoleRel> createAccountRoleRel(AccountRoleRel value) {
        return new JAXBElement<>(_AccountRoleRel_QNAME, AccountRoleRel.class, null, value);
    }

    @XmlElementDecl(namespace = AUTHORIZATION_NS, name = "account_permission")
    public JAXBElement<AccountPermission> createAccountPermission(AccountPermission value) {
        return new JAXBElement<>(_AccountPermission_QNAME, AccountPermission.class, null, value);
    }

    @XmlElementDecl(namespace = AUTHORIZATION_NS, name = "permission_role_rel")
    public JAXBElement<PermissionRoleRel> createPermissionRoleRel(PermissionRoleRel value) {
        return new JAXBElement<>(_PermissionRoleRel_QNAME, PermissionRoleRel.class, null, value);
    }

    @XmlElementDecl(namespace = AUTHORIZATION_NS, name = "role")
    public JAXBElement<Role> createRole(Role value) {
        return new JAXBElement<>(_Role_QNAME, Role.class, null, value);
    }

    @XmlElementDecl(namespace = AUTHORIZATION_NS, name = "roles_list")
    public JAXBElement<RolesList> createRolesList(RolesList value) {
        return new JAXBElement<>(_RolesList_QNAME, RolesList.class, null, value);
    }

    @XmlElementDecl(namespace = AUTHORIZATION_NS, name = "permission_role")
    public JAXBElement<PermissionRole> createPermissionRole(PermissionRole value) {
        return new JAXBElement<>(_PermissionRole_QNAME, PermissionRole.class, null, value);
    }
}
