/**
 * This document is a part of the source code and related artifacts for
 * CollectionSpace, an open source collections management system for museums and
 * related institutions:
 *
 * http://www.collectionspace.org http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0. You may
 * not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.collectionspace.services.common.api.test;

import org.collectionspace.services.common.api.RefName;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * User: laramie $LastChangedRevision: $ $LastChangedDate: $
 */
public class RefNameTest {

    public static final String AUTHORITY_EXAMPLE = "urn:cspace:collectionspace.org:Loansin:name(shortID)'displayName'";
    public static final String AUTHORITY_EXAMPLE2 = "urn:cspace:collectionspace.org:Loansin:name(shortID)";
    public static final String AUTHORITY_ITEM_EXAMPLE = "urn:cspace:collectionspace.org:Loansin:name(shortID):item:name(itemShortID)'itemDisplayName'";
    public static final String EX_tenantName = "collectionspace.org";
    public static final String EX_resource = "Loansin";
    public static final String EX_shortIdentifier = "shortID";
    public static final String EX_displayName = "displayName";
    public static final String EX_itemShortIdentifier = "itemShortID";
    public static final String EX_itemDisplayName = "itemDisplayName";
    public static final String EX_itemDisplayNameContainsColon = "itemDisplayName:itemDisplayName2:itemDisplayName3";
    public static final String EX_itemDisplayNameContainsParens = "itemDisplayName (in parens) and more";
    

    private void testBanner(String msg) {
        String BANNER = "-------------------------------------------------------";
        System.out.println(BANNER + "\r\n" + this.getClass().getName() + "\r\n" + msg + "\r\n" + BANNER);
    }

    private void check(String one, String two, String msg) {
        if (one == null || two == null || !(one.equals(two))) {
            System.out.println("equals check FAILED. (msg: " + msg + ")\r\none: " + one + "\r\ntwo: " + two + "\r\n");
        } else {
            System.out.println("equals check OK. (msg: " + msg + ")\r\none: " + one + "\r\ntwo: " + two + "\r\n");
        }
        Assert.assertEquals(one, two, msg);
    }

    @Test
    public void testRoundTrip() {
        testBanner("testRoundTrip");
        RefName.AuthorityItem item = RefName.AuthorityItem.parse(AUTHORITY_ITEM_EXAMPLE);
        check(item.displayName, EX_itemDisplayName, "EX_itemDisplayName");
        check(item.shortIdentifier, EX_itemShortIdentifier, "EX_itemShortIdentifier");
        check(item.toString(), AUTHORITY_ITEM_EXAMPLE, "AUTHORITY_ITEM_EXAMPLE");
        check(item.inAuthority.displayName, "", "displayName from inAuthority should be empty");

        RefName.Authority authority = RefName.Authority.parse(AUTHORITY_EXAMPLE);
        check(authority.toString(), AUTHORITY_EXAMPLE, "AUTHORITY_ITEM_EXAMPLE");
        check(authority.tenantName, EX_tenantName, "EX_tenantName");
        check(authority.resource, EX_resource, "EX_resource");
        check(authority.displayName, EX_displayName, "EX_displayName");
        check(authority.shortIdentifier, EX_shortIdentifier, "EX_shortIdentifier");

        Assert.assertEquals(authority, item.inAuthority, "inAuthority from AuthorityItem.parse() matches Authority from Authority.parse()");
    }

    @Test
    public void testConstructors() {
        testBanner("testConstructors");
        RefName.AuthorityItem item = RefName.buildAuthorityItem(AUTHORITY_EXAMPLE2,
                EX_itemShortIdentifier,
                EX_itemDisplayName);
        RefName.AuthorityItem itemParsed = RefName.AuthorityItem.parse(AUTHORITY_ITEM_EXAMPLE);
        check(item.toString(), itemParsed.toString(), "buildAuthorityItem from AUTHORITY_EXAMPLE2 vs. parse(AUTHORITY_ITEM_EXAMPLE)");
        Assert.assertEquals(item, itemParsed);



        RefName.Authority authority2 = RefName.Authority.parse(AUTHORITY_EXAMPLE2);
        RefName.AuthorityItem item3 = RefName.buildAuthorityItem(authority2,
                EX_itemShortIdentifier,
                EX_itemDisplayName);
        check(item.toString(), item3.toString(), "buildAuthorityItem(Authority,str,str) from AUTHORITY_EXAMPLE2 vs. AUTHORITY_ITEM_EXAMPLE");
        Assert.assertEquals(item, item3);
    }

    /**
     * Test convenience getters that return short identifiers for authorities
     * and authority items.
     */
    @Test
    public void testShortIDGetters() {
        testBanner("testShortIDGetters");
        RefName.Authority authority = RefName.Authority.parse(AUTHORITY_EXAMPLE);
        Assert.assertEquals(authority.getShortIdentifier(), EX_shortIdentifier,
                "Short identifier from parsing parent authority refName does not match value of Authority.getShortIdentifier().");

        RefName.AuthorityItem item = RefName.buildAuthorityItem(AUTHORITY_EXAMPLE,
                EX_itemShortIdentifier,
                EX_itemDisplayName);
        Assert.assertEquals(item.getParentShortIdentifier(), EX_shortIdentifier,
                "Parent short identifier from parsing authority refName does not match value of AuthorityItem.getParentShortIdentifier().");
        Assert.assertEquals(item.getShortIdentifier(), EX_itemShortIdentifier,
                "Short identifier from item does not match value of AuthorityItem.getShortIdentifier().");

        RefName.AuthorityItem parsedItem = RefName.AuthorityItem.parse(AUTHORITY_ITEM_EXAMPLE);
        Assert.assertEquals(parsedItem.getParentShortIdentifier(), EX_shortIdentifier,
                "Parent short identifier from parsing item refName does not match value of AuthorityItem.getParentShortIdentifier().");
        Assert.assertEquals(parsedItem.getShortIdentifier(), EX_itemShortIdentifier,
                "Short identifier from parsing item refName does not match value of AuthorityItem.getShortIdentifier().");

    }

    /**
     * Test display names containing characters which are also used as separators in refNames.
     */
    @Test
    public void testSeparatorsInDisplayNames() {
        testBanner("testSeparatorsInDisplayNames");
        
        RefName.AuthorityItem itemDisplayNameContainsColon =
                RefName.buildAuthorityItem(AUTHORITY_EXAMPLE2,
                EX_itemShortIdentifier,
                EX_itemDisplayNameContainsColon);
        Assert.assertEquals(itemDisplayNameContainsColon.getShortIdentifier(), EX_itemShortIdentifier,
                "Short identifier from item " + itemDisplayNameContainsColon.getShortIdentifier()
                + "does not match expected value " + EX_itemShortIdentifier);
        Assert.assertEquals(itemDisplayNameContainsColon.displayName, EX_itemDisplayNameContainsColon,
                "Short identifier from item " + itemDisplayNameContainsColon.displayName
                + "does not match expected value " + EX_itemDisplayNameContainsColon);
        
        RefName.AuthorityItem itemDisplayNameContainsParens =
                RefName.buildAuthorityItem(AUTHORITY_EXAMPLE2,
                EX_itemShortIdentifier,
                EX_itemDisplayNameContainsParens);
        Assert.assertEquals(itemDisplayNameContainsParens.getShortIdentifier(), EX_itemShortIdentifier,
                "Short identifier from item " + itemDisplayNameContainsParens.getShortIdentifier()
                + "does not match expected value " + EX_itemShortIdentifier);
        Assert.assertEquals(itemDisplayNameContainsParens.displayName, EX_itemDisplayNameContainsParens,
                "Short identifier from item " + itemDisplayNameContainsParens.displayName
                + "does not match expected value " + EX_itemDisplayNameContainsParens);
    }
}
