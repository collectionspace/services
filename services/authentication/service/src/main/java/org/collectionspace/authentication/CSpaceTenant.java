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
package org.collectionspace.authentication;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A CSpace Principal representing a tenant
 * A class derived (in principle) from JBoss SimpleGroup and SimplePrincipal
 * @author 
 */
public class CSpaceTenant implements Group, Cloneable {

    /** The serialVersionUID */
    private static final long serialVersionUID = 1L;
    private String name;
    private String id;
    private HashMap<Principal, Principal> members = new HashMap<Principal, Principal>();

    public CSpaceTenant(String name, String id) {
        if(name == null || id == null) {
            String msg = "CSpaceTenant: invalid argument(s), can't be null" +
                    "name=" + name + " id=" + id;
            throw new IllegalArgumentException(msg);
        }
        this.name = name;
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    /** 
     * Adds the specified member to the tenant.
     * @param user the principal to add to this tenant.
     * @return true if the member was successfully added,
     * false if the principal was already a member.
     */
    @Override
    public boolean addMember(Principal user) {
        boolean isMember = members.containsKey(user);
        if (isMember == false) {
            members.put(user, user);
        }
        return isMember == false;
    }

    /** 
     * isMember returns true if the passed principal is a member of the tenant.
     * This method does a recursive search, so if a principal belongs to a
     * tenant which is a member of this tenant, true is returned.
     * @param member the principal whose membership is to be checked.
     * @return true if the principal is a member of this tenant, false otherwise.
     */
    @Override
    public boolean isMember(Principal member) {
        // First see if there is a key with the member name
        boolean isMember = members.containsKey(member);
        if (isMember == false) {   // Check any tenants for membership
            Collection values = members.values();
            Iterator iter = values.iterator();
            while (isMember == false && iter.hasNext()) {
                Object next = iter.next();
                if (next instanceof Group) {
                    Group tenant = (Group) next;
                    isMember = tenant.isMember(member);
                }
            }
        }
        return isMember;
    }

    /** 
     * members returns an enumeration of the members in the tenant.
     * The returned objects can be instances of either Principal
     * or Group (which is a subinterface of Principal).
     * @return an enumeration of the tenant members.
     */
    @Override
    public Enumeration members() {
        return Collections.enumeration(members.values());
    }

    /** 
     * removeMember removes the specified member from the tenant.
     * @param user the principal to remove from this tenant.
     * @return true if the principal was removed, or
     * false if the principal was not a member.
     */
    @Override
    public boolean removeMember(Principal user) {
        Object prev = members.remove(user);
        return prev != null;
    }

    /**
     * Compare this tenant against another tenant
     * @return true if name equals another.getName();
     */
    @Override
    public boolean equals(Object another) {
        if (!(another instanceof CSpaceTenant)) {
            return false;
        }
        String anotherName = ((CSpaceTenant) another).getName();
        String anotherId = ((CSpaceTenant) another).getId();
        return name.equals(anotherName) && id.equals(anotherId);
    }

    @Override
    public int hashCode() {
        return (name + id).hashCode();
    }

    @Override
    public String toString() {
        StringBuffer tmp = new StringBuffer(getName());
        tmp.append("(members:");
        Iterator iter = members.keySet().iterator();
        while (iter.hasNext()) {
            tmp.append(iter.next());
            tmp.append(',');
        }
        tmp.setCharAt(tmp.length() - 1, ')');
        return tmp.toString();
    }

    @Override
    public synchronized Object clone() throws CloneNotSupportedException {
        CSpaceTenant clone = (CSpaceTenant) super.clone();
        if (clone != null) {
            clone.members = (HashMap) this.members.clone();
        }
        return clone;
    }
}
