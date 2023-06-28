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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.collectionspace.authentication.jackson2.CSpaceTenantDeserializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A CollectionSpace tenant.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonDeserialize(using = CSpaceTenantDeserializer.class)
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CSpaceTenant {
    private final String id;
    private final String name;

    /**
     * Creates a CSpaceTenant with a given id and name.
     *
     * @param id the tenant id, e.g. "1"
     * @param name the tenant name, e.g. "core.collectionspace.org"
     */
    public CSpaceTenant(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int hashCode() {
        // The tenant id uniquely identifies the tenant,
        // regardless of other properties. CSpaceTenants
        // with the same id should hash identically.

        return new HashCodeBuilder(83, 61)
            .append(id)
            .build();
    }

    @Override
    public boolean equals(Object obj) {
        // The tenant id uniquely identifies the tenant,
        // regardless of other properties. CSpaceTenants
        // with the same id should be equal.

        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj.getClass() != getClass()) {
          return false;
        }

        CSpaceTenant rhs = (CSpaceTenant) obj;

        return new EqualsBuilder()
           .append(id, rhs.getId())
           .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
            append("id", id).
            append("name", name).
            toString();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
