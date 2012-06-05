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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * User: laramie $LastChangedRevision: $ $LastChangedDate: $
 */
public class RefNameTest {
    
    private static final Logger logger = LoggerFactory.getLogger(RefName.class);

    public static final String AUTHORITY_EXAMPLE = "urn:cspace:collectionspace.org:Loansin:name(shortID)'displayName'";
    public static final String AUTHORITY_EXAMPLE2 = "urn:cspace:collectionspace.org:Loansin:name(shortID)";
    public static final String AUTHORITY_ITEM_EXAMPLE = "urn:cspace:collectionspace.org:Loansin:name(shortID):item:name(itemShortID)'itemDisplayName'";
    public static final String TENANT_DOMAIN_NAME = "collectionspace.org";
    public static final String RESOURCE = "Loansin";
    public static final String SHORT_IDENTIFIER = "shortID";
    public static final String DISPLAY_NAME = "displayName";
    public static final String ITEM_SHORT_IDENTIFIER = "itemShortID";
    public static final String ITEM_DISPLAY_NAME = "itemDisplayName";
    public static final String DISPLAY_NAME_WITH_COLONS = "itemDisplayName:itemDisplayName2:itemDisplayName3";
    public static final String DISPLAY_NAME_WITH_PARENS = "itemDisplayName (in parens) and more";
    public static final String DISPLAY_NAME_WITH_APOSTROPHE = "itemDisplayName O'Reilly";

    

    private void testBanner(String msg) {
        String BANNER = "-------------------------------------------------------";
        logger.debug("\r" + BANNER + "\r\n" + this.getClass().getName() + "\r\n" + msg + "\r\n" + BANNER);
    }

    private void check(String one, String two, String msg) {
        if (one == null || two == null || !(one.equals(two))) {
            logger.debug("equals check FAILED. (msg: " + msg + ")\r\none: " + one + "\r\ntwo: " + two + "\r\n");
        } else {
            logger.debug("equals check OK. (msg: " + msg + ")\r\none: " + one + "\r\ntwo: " + two + "\r\n");
        }
        Assert.assertEquals(one, two, msg);
    }

    @Test
    public void testRoundTrip() {
        testBanner("testRoundTrip");
        RefName.AuthorityItem item = RefName.AuthorityItem.parse(AUTHORITY_ITEM_EXAMPLE);
        check(item.toString(), AUTHORITY_ITEM_EXAMPLE, "Item refName");
        check(item.displayName, ITEM_DISPLAY_NAME, "Item display name");
        check(item.shortIdentifier, ITEM_SHORT_IDENTIFIER, "Item short identifier");
        check(item.inAuthority.displayName, "", "displayName from inAuthority should be empty");

        RefName.Authority authority = RefName.Authority.parse(AUTHORITY_EXAMPLE);
        check(authority.toString(), AUTHORITY_EXAMPLE, "Authority refName");
        check(authority.tenantName, TENANT_DOMAIN_NAME, "Authority tenant domain name");
        check(authority.resource, RESOURCE, "Authority resource");
        check(authority.displayName, DISPLAY_NAME, "Authority display name");
        check(authority.shortIdentifier, SHORT_IDENTIFIER, "Authority short identifier");

        Assert.assertEquals(authority, item.inAuthority, "inAuthority from AuthorityItem.parse() matches Authority from Authority.parse()");
    }

    @Test
    public void testConstructors() {
        testBanner("testConstructors");
        RefName.AuthorityItem item = RefName.buildAuthorityItem(AUTHORITY_EXAMPLE2,
                ITEM_SHORT_IDENTIFIER,
                ITEM_DISPLAY_NAME);
        RefName.AuthorityItem itemParsed = RefName.AuthorityItem.parse(AUTHORITY_ITEM_EXAMPLE);
        check(item.toString(), itemParsed.toString(), "buildAuthorityItem from AUTHORITY_EXAMPLE2 vs. parse(AUTHORITY_ITEM_EXAMPLE)");
        Assert.assertEquals(item, itemParsed);



        RefName.Authority authority2 = RefName.Authority.parse(AUTHORITY_EXAMPLE2);
        RefName.AuthorityItem item3 = RefName.buildAuthorityItem(authority2,
                ITEM_SHORT_IDENTIFIER,
                ITEM_DISPLAY_NAME);
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
        Assert.assertEquals(authority.getShortIdentifier(), SHORT_IDENTIFIER,
                "Short identifier from parsing parent authority refName does not match value of Authority.getShortIdentifier().");

        RefName.AuthorityItem item = RefName.buildAuthorityItem(AUTHORITY_EXAMPLE,
                ITEM_SHORT_IDENTIFIER,
                ITEM_DISPLAY_NAME);
        Assert.assertEquals(item.getParentShortIdentifier(), SHORT_IDENTIFIER,
                "Parent short identifier from parsing authority refName does not match value of AuthorityItem.getParentShortIdentifier().");
        Assert.assertEquals(item.getShortIdentifier(), ITEM_SHORT_IDENTIFIER,
                "Short identifier from item does not match value of AuthorityItem.getShortIdentifier().");

        RefName.AuthorityItem parsedItem = RefName.AuthorityItem.parse(AUTHORITY_ITEM_EXAMPLE);
        Assert.assertEquals(parsedItem.getParentShortIdentifier(), SHORT_IDENTIFIER,
                "Parent short identifier from parsing item refName does not match value of AuthorityItem.getParentShortIdentifier().");
        Assert.assertEquals(parsedItem.getShortIdentifier(), ITEM_SHORT_IDENTIFIER,
                "Short identifier from parsing item refName does not match value of AuthorityItem.getShortIdentifier().");

    }

    /**
     * Test display names containing refName separator characters, such as
     * colons and parens, to ensure that these characters may be validly used
     * in display names without adversely impacting refName parsing.
     */
    @Test
    public void testSeparatorsInDisplayNames() {
        testBanner("testSeparatorsInDisplayNames");
        
        RefName.AuthorityItem itemWithDisplayNameColons =
                RefName.buildAuthorityItem(AUTHORITY_EXAMPLE2,
                ITEM_SHORT_IDENTIFIER,
                DISPLAY_NAME_WITH_COLONS);
        Assert.assertEquals(itemWithDisplayNameColons.getShortIdentifier(), ITEM_SHORT_IDENTIFIER,
                "Short identifier from item " + itemWithDisplayNameColons.getShortIdentifier()
                + "does not match expected value " + ITEM_SHORT_IDENTIFIER);
        Assert.assertEquals(itemWithDisplayNameColons.displayName, DISPLAY_NAME_WITH_COLONS,
                "Display name from item " + itemWithDisplayNameColons.displayName
                + "does not match expected value " + DISPLAY_NAME_WITH_COLONS);
        
        RefName.AuthorityItem itemWithDisplayNameParens =
                RefName.buildAuthorityItem(AUTHORITY_EXAMPLE2,
                ITEM_SHORT_IDENTIFIER,
                DISPLAY_NAME_WITH_PARENS);
        Assert.assertEquals(itemWithDisplayNameParens.getShortIdentifier(), ITEM_SHORT_IDENTIFIER,
                "Short identifier from item " + itemWithDisplayNameParens.getShortIdentifier()
                + "does not match expected value " + ITEM_SHORT_IDENTIFIER);
        Assert.assertEquals(itemWithDisplayNameParens.displayName, DISPLAY_NAME_WITH_PARENS,
                "Display name from item " + itemWithDisplayNameParens.displayName
                + "does not match expected value " + DISPLAY_NAME_WITH_PARENS);
        
        RefName.AuthorityItem itemWithDisplayNameApostrophe =
                RefName.buildAuthorityItem(AUTHORITY_EXAMPLE2,
                ITEM_SHORT_IDENTIFIER,
                DISPLAY_NAME_WITH_APOSTROPHE);
        Assert.assertEquals(itemWithDisplayNameApostrophe.getShortIdentifier(), ITEM_SHORT_IDENTIFIER,
                "Short identifier from item " + itemWithDisplayNameApostrophe.getShortIdentifier()
                + "does not match expected value " + ITEM_SHORT_IDENTIFIER);
        Assert.assertEquals(itemWithDisplayNameApostrophe.displayName, DISPLAY_NAME_WITH_APOSTROPHE,
                "Display name from item " + itemWithDisplayNameApostrophe.displayName
                + "does not match expected value " + DISPLAY_NAME_WITH_APOSTROPHE);
    }
}
