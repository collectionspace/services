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
package org.collectionspace.services.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.collectionspace.services.authorization.perms.ActionType;
import org.collectionspace.services.authorization.perms.EffectType;
import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.PermissionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class PermissionFactory {

    static private final Logger logger =
            LoggerFactory.getLogger(PermissionFactory.class);

    /**
     * create permission instance
     * @param resourceName
     * @param description
     * @param actionList list of actions for this permission
     * @param effect effect of the permission
     * @param useResourceName
     * @param useAction
     * @param useEffect
     * @return
     */
    public static Permission createPermissionInstance(String resourceName,
            String description,
            List<PermissionAction> actionList,
            EffectType effect,
            boolean useResourceName,
            boolean useAction,
            boolean useEffect) {

        Permission permission = new Permission();
        
        if (useResourceName) {
            permission.setResourceName(resourceName);
        }
        if (useAction) {
            permission.setAction(actionList);
            String actionGroup = PermissionClient.getActionGroup(actionList);
            permission.setActionGroup(actionGroup);
        }
        if (useEffect) {
            permission.setEffect(effect);
        }
        return permission;
    }

	public static String createDefaultActionGroup() {
    	return "CRUDL";
    }

    public static List<PermissionAction> createDefaultActions() {
        List<PermissionAction> actions = new ArrayList<PermissionAction>();
        
        actions.add(PermissionActionFactory.create(ActionType.CREATE));
        actions.add(PermissionActionFactory.create(ActionType.READ));
        actions.add(PermissionActionFactory.create(ActionType.UPDATE));
        actions.add(PermissionActionFactory.create(ActionType.DELETE));
        actions.add(PermissionActionFactory.create(ActionType.SEARCH));

        return actions;
    }
}
