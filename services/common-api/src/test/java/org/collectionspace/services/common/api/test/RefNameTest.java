/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.common.api.test;

import org.collectionspace.services.common.api.RefName;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
 public class RefNameTest {

    private void testBanner(String msg){
        String BANNER ="-------------------------------------------------------";
        System.out.println(BANNER+"\r\n"+this.getClass().getName()+"\r\n"+msg+"\r\n"+BANNER);
    }

    private void check(String one, String two, String msg){
        if (one == null || two == null || !(one.equals(two)) ){
            System.out.println("equals check FAILED. (msg: "+msg+")\r\none: "+one+"\r\ntwo: "+two+"\r\n");
        } else {
            System.out.println("equals check OK. (msg: "+msg+")\r\none: "+one+"\r\ntwo: "+two+"\r\n");
        }
        Assert.assertEquals(one, two, msg);
    }

    @Test
    public void testRoundTrip(){
        testBanner("testRoundTrip");
        RefName.AuthorityItem item = RefName.AuthorityItem.parse(RefName.AUTHORITY_ITEM_EXAMPLE);
        check(item.displayName,     RefName.EX_itemDisplayName, "EX_itemDisplayName");
        check(item.shortIdentifier, RefName.EX_itemShortIdentifier, "EX_itemShortIdentifier");
        check(item.toString(),      RefName.AUTHORITY_ITEM_EXAMPLE, "AUTHORITY_ITEM_EXAMPLE");
        check(item.inAuthority.displayName,    "", "displayName from inAuthority should be empty");

        RefName.Authority authority = RefName.Authority.parse(RefName.AUTHORITY_EXAMPLE);
        check(authority.toString(),     RefName.AUTHORITY_EXAMPLE, "AUTHORITY_ITEM_EXAMPLE");
        check(authority.tenantName,     RefName.EX_tenantName, "EX_tenantName");
        check(authority.resource,       RefName.EX_resource, "EX_resource");
        check(authority.displayName,    RefName.EX_displayName, "EX_displayName");
        check(authority.shortIdentifier,RefName.EX_shortIdentifier, "EX_shortIdentifier");

        Assert.assertEquals(authority, item.inAuthority, "inAuthority from AuthorityItem.parse() matches Authority from Authority.parse()");
    }

    @Test
    public void testConstructors(){
        testBanner("testConstructors");
        RefName.AuthorityItem item = RefName.buildAuthorityItem(RefName.AUTHORITY_EXAMPLE2,
                                                                RefName.EX_itemShortIdentifier,
                                                                RefName.EX_itemDisplayName);
        RefName.AuthorityItem itemParsed = RefName.AuthorityItem.parse(RefName.AUTHORITY_ITEM_EXAMPLE);
        check(item.toString(), itemParsed.toString(), "buildAuthorityItem from AUTHORITY_EXAMPLE2 vs. parse(AUTHORITY_ITEM_EXAMPLE)");
        Assert.assertEquals(item, itemParsed);



        RefName.Authority authority2 = RefName.Authority.parse(RefName.AUTHORITY_EXAMPLE2);
        RefName.AuthorityItem item3 = RefName.buildAuthorityItem(authority2,
                                                                RefName.EX_itemShortIdentifier,
                                                                RefName.EX_itemDisplayName);
        check(item.toString(), item3.toString(), "buildAuthorityItem(Authority,str,str) from AUTHORITY_EXAMPLE2 vs. AUTHORITY_ITEM_EXAMPLE");
        Assert.assertEquals(item, item3);
    }
    
    /**
     * Test convenience getters that return short identifiers for authorities and authority items.
     */
    @Test
    public void testShortIDGetters(){
        testBanner("testShortIDGetters");
        RefName.Authority authority = RefName.Authority.parse(RefName.AUTHORITY_EXAMPLE);
        Assert.assertEquals(authority.getShortIdentifier(), RefName.EX_shortIdentifier,
                "Short identifier from parsing parent authority refName does not match value of Authority.getShortIdentifier().");
        
        RefName.AuthorityItem item = RefName.buildAuthorityItem(RefName.AUTHORITY_EXAMPLE,
                                                                RefName.EX_itemShortIdentifier,
                                                                RefName.EX_itemDisplayName);
        Assert.assertEquals(item.getParentShortIdentifier(), RefName.EX_shortIdentifier,
              "Parent short identifier from parsing authority refName does not match value of AuthorityItem.getParentShortIdentifier().");
        Assert.assertEquals(item.getShortIdentifier(), RefName.EX_itemShortIdentifier,
              "Short identifier from item does not match value of AuthorityItem.getShortIdentifier().");
        
        RefName.AuthorityItem parsedItem = RefName.AuthorityItem.parse(RefName.AUTHORITY_ITEM_EXAMPLE);
        Assert.assertEquals(parsedItem.getParentShortIdentifier(), RefName.EX_shortIdentifier,
              "Parent short identifier from parsing item refName does not match value of AuthorityItem.getParentShortIdentifier().");
        Assert.assertEquals(parsedItem.getShortIdentifier(), RefName.EX_itemShortIdentifier,
              "Short identifier from parsing item refName does not match value of AuthorityItem.getShortIdentifier().");

    }

}
