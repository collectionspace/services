/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.authorization.importer;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.collectionspace.services.authorization.AccountRole;
import org.collectionspace.services.authorization.ActionType;
import org.collectionspace.services.authorization.Permission;
import org.collectionspace.services.authorization.EffectType;
import org.collectionspace.services.authorization.PermissionAction;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.PermissionsList;
import org.collectionspace.services.authorization.PermissionsRolesList;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.SubjectType;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.tenant.TenantBindingType;

/**
 * AuthorizationGen generates authorizations (permissions and roles)
 * for tenant services
 * @author 
 */
public class AuthorizationGen {

    final Logger logger = LoggerFactory.getLogger(AuthorizationGen.class);
    private List<Permission> permList = new ArrayList<Permission>();
    private List<PermissionRole> permRoleList = new ArrayList<PermissionRole>();
    private Hashtable<String, TenantBindingType> tenantBindings =
            new Hashtable<String, TenantBindingType>();
    final public static String ROLE_ADMINISTRATOR = "ROLE_ADMINISTRATOR";

    public void initialize(String tenantBindingFileName) throws Exception {
        TenantBindingConfigReaderImpl tenantBindingConfigReader =
                new TenantBindingConfigReaderImpl(null);
        tenantBindingConfigReader.read(tenantBindingFileName);
        tenantBindings = tenantBindingConfigReader.getTenantBindings();
        if (logger.isDebugEnabled()) {
            logger.debug("initialized with tenant bindings from " + tenantBindingFileName);
        }
    }

    public void createDefaultServicePermissions() {
        for (String tenantId : tenantBindings.keySet()) {
            List<Permission> perms = createDefaultServicePermissions(tenantId);
            permList.addAll(perms);
        }
    }

    public List<Permission> createDefaultServicePermissions(String tenantId) {
        ArrayList<Permission> apcList = new ArrayList<Permission>();
        TenantBindingType tbinding = tenantBindings.get(tenantId);
        for (ServiceBindingType sbinding : tbinding.getServiceBindings()) {

            //add permissions for the main path
            Permission perm = buildCommonPermission(tbinding.getId(),
                    sbinding.getName().toLowerCase());
            apcList.add(perm);

            //add permissions for alternate paths
            List<String> uriPaths = sbinding.getUriPath();
            for (String uriPath : uriPaths) {
                perm = buildCommonPermission(tbinding.getId(),
                        uriPath.toLowerCase());
                apcList.add(perm);
            }

        }
        return apcList;

    }

    private Permission buildCommonPermission(String tenantId, String resourceName) {
        String id = UUID.randomUUID().toString();
        Permission perm = new Permission();
        perm.setCsid(id);
        perm.setResourceName(resourceName.toLowerCase());
        perm.setEffect(EffectType.PERMIT);
        perm.setTenantId(tenantId);
        ArrayList<PermissionAction> pas = new ArrayList<PermissionAction>();
        perm.setActions(pas);

        PermissionAction pa = new PermissionAction();
        pa.setName(ActionType.CREATE);
        pas.add(pa);
        PermissionAction pa1 = new PermissionAction();
        pa1.setName(ActionType.READ);
        pas.add(pa1);
        PermissionAction pa2 = new PermissionAction();
        pa2.setName(ActionType.UPDATE);
        pas.add(pa2);
        PermissionAction pa3 = new PermissionAction();
        pa3.setName(ActionType.DELETE);
        pas.add(pa3);
        PermissionAction pa4 = new PermissionAction();
        pa4.setName(ActionType.SEARCH);
        pas.add(pa4);
        return perm;
    }

    public List<Permission> getDefaultServicePermissions() {
        return permList;
    }

    public void createDefaultPermissionsRoles() {
        for (Permission p : permList) {
            TenantBindingType tbinding = tenantBindings.get(p.getTenantId());
//            String tenantAdminRole = getTenantAdminRole(tbinding.getName());
//            PermissionRole permRole = buildCommonPermissionRoles(p.getTenantId(), p.getCsid(),
//                    p.getResourceName(), tenantAdminRole, "999");
//            permRoleList.add(permRole);

            //CSpace Administrator has all access
            PermissionRole permAdmRole = buildCommonPermissionRoles(p.getTenantId(), p.getCsid(),
                    p.getResourceName(), ROLE_ADMINISTRATOR, "1");
            permRoleList.add(permAdmRole);
        }
    }

    public List<PermissionRole> createPermissionsRoles(List<Permission> perms, String roleName, String roleId) {
        List<PermissionRole> permRoles = new ArrayList<PermissionRole>();
        for (Permission p : perms) {
            PermissionRole permRole = buildCommonPermissionRoles(p.getTenantId(), p.getCsid(),
                    p.getResourceName(), roleName, roleId);
            permRoles.add(permRole);
        }
        return permRoles;
    }

    private PermissionRole buildCommonPermissionRoles(String tenantId, String permId,
            String resName, String roleName, String roleId) {

        PermissionRole pr = new PermissionRole();
        pr.setSubject(SubjectType.ROLE);
        List<PermissionValue> permValues = new ArrayList<PermissionValue>();
        pr.setPermissions(permValues);
        PermissionValue permValue = new PermissionValue();
        permValue.setPermissionId(permId);
        permValue.setResourceName(resName.toLowerCase());
        permValues.add(permValue);

        List<RoleValue> roleValues = new ArrayList<RoleValue>();
        RoleValue radmin = new RoleValue();
        radmin.setRoleName(roleName.toUpperCase());
        radmin.setRoleId(roleId);
        roleValues.add(radmin);
        pr.setRoles(roleValues);

        return pr;
    }

    /**
     * getTenantAdminRole generates role for tenant administrator
     * @param tenantName
     * @return
     */
    private String getTenantAdminRole(String tenantName) {
        tenantName = tenantName.toUpperCase();
        tenantName = tenantName.replace(' ', '_');
        return ROLE_ADMINISTRATOR + "_" + tenantName;
    }

    public List<PermissionRole> getDefaultServicePermissionRoles() {
        return permRoleList;
    }

    public void exportPermissions(String fileName) {
        PermissionsList pcList = new PermissionsList();
        pcList.setPermissions(permList);
        toFile(pcList, PermissionsList.class,
                fileName);
        if (logger.isDebugEnabled()) {
            logger.debug("exported permissions to " + fileName);
        }
    }

    public void exportPermissionRoles(String fileName) {
        PermissionsRolesList psrsl = new PermissionsRolesList();
        psrsl.setPermissionRoles(permRoleList);
        toFile(psrsl, PermissionsRolesList.class,
                fileName);
        if (logger.isDebugEnabled()) {
            logger.debug("exported permissions-roles to " + fileName);
        }
    }

    private void toFile(Object o, Class jaxbClass, String fileName) {
        File f = new File(fileName);
        try {
            JAXBContext jc = JAXBContext.newInstance(jaxbClass);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(o, f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
