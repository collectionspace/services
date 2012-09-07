/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2011 University of California at Berkeley

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

package org.collectionspace.services.common.document;

import org.collectionspace.services.client.IRelationsManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.XmlTools;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.relation.RelationResource;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationsDocListItem;
import org.collectionspace.services.relation.RelationshipType;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

/**
 * @author Laramie Crocker
 */
public class Hierarchy {

    public static final String directionQP = "direction";
    public static final String direction_parents = "parents";


    /**Call with the URI and CSID of the root element of the tree you wish to inspect.  The uri can be a blank string.
     * @param uri informational, optional - if not known, pass an empty String.
     * @return String of XML document, including xml processing instruction, root node is "&lt;hierarchy&gt;".
     */
    public static String dive(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String itemcsid, String uri) {
        String result = dive(ctx, itemcsid, uri, true);
        result =  "<?xml version='1.0' ?><hierarchy>"+result+"</hierarchy>";
        try {
            result = XmlTools.prettyPrint(result);
        } catch (Exception e){
        	// Do nothing
        }
        return result;
    }

    private static String dive(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String itemcsid, String uri, boolean lookupFirstName) {
        MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
        //Run getList() once as sent to get childListOuter:
        queryParams.putSingle(IRelationsManager.PREDICATE_QP, RelationshipType.HAS_BROADER.value());
        queryParams.putSingle(IRelationsManager.SUBJECT_QP, null);
        queryParams.putSingle(IRelationsManager.SUBJECT_TYPE_QP, null);
        queryParams.putSingle(IRelationsManager.OBJECT_QP, itemcsid);
        queryParams.putSingle(IRelationsManager.OBJECT_TYPE_QP, null);
        
        RelationResource relationResource = new RelationResource();
        RelationsCommonList childListOuter = relationResource.getList(ctx);    // Knows all query params because they are in the context.
        List<RelationsCommonList.RelationListItem> childList = childListOuter.getRelationListItem();

        StringBuffer sb = new StringBuffer();

        if (lookupFirstName && childList.size() > 0) {
            RelationsCommonList.RelationListItem firstItem = childList.get(0);
            sb.append("<uri>" + firstItem.getObject().getUri() + "</uri>\r\n");
            sb.append("<uri-called>" + uri + "</uri-called>\r\n");
            sb.append("<name>" + firstItem.getObject().getName() + "</name><number>" + firstItem.getObject().getNumber() + "</number>\r\n");
        } else {
            sb.append("<uri>" + uri + "</uri>\r\n");
        }
        
        sb.append("<csid>" + itemcsid + "</csid>\r\n");
        sb.append("<children>\r\n");
        
        for (RelationsCommonList.RelationListItem item : childList) {
            RelationsDocListItem parent = item.getObject();
            RelationsDocListItem child = item.getSubject();
            String childCSID = child.getCsid();
            String childURI = child.getUri();
            sb.append("<child>\r\n");
            sb.append("<parent-uri>" +parent.getUri() + "</parent-uri>\r\n");
            sb.append("  <name>" + child.getName() + "</name><number>" + child.getNumber() + "</number>\r\n");
            String s = dive(ctx, childCSID, childURI, false);
            sb.append(s);
            sb.append("</child>\r\n");
        }
        sb.append("</children>\r\n");
        return sb.toString();
    }

    public static String surface(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String itemcsid, String uri) {
        String result = surface(ctx, itemcsid, uri, true).resultBuffer.toString();
        result =  "<?xml version='1.0' ?><hierarchy direction='"+direction_parents+"'>"+result+"</hierarchy>";
        try {
            result = XmlTools.prettyPrint(result);
        } catch (Exception e){
        	// Do nothing
        }
        return result;
    }
    
    private static class SurfaceResultStruct {
        public StringBuffer resultBuffer;
        public boolean noParents = false;
    }
    
    private static SurfaceResultStruct surface(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String itemcsid, String uri, boolean first) {
        MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
        //Run getList() once as sent to get parentListOuter:
        queryParams.putSingle(IRelationsManager.PREDICATE_QP, RelationshipType.HAS_BROADER.value());
        queryParams.putSingle(IRelationsManager.SUBJECT_QP, itemcsid);
        queryParams.putSingle(IRelationsManager.SUBJECT_TYPE_QP, null);
        queryParams.putSingle(IRelationsManager.OBJECT_QP, null);
        queryParams.putSingle(IRelationsManager.OBJECT_TYPE_QP, null);
        
        RelationResource relationResource = new RelationResource();
        RelationsCommonList parentListOuter = relationResource.getList(ctx);    // Knows all query params because they are in the context.
        List<RelationsCommonList.RelationListItem> parentList = parentListOuter.getRelationListItem();

        StringBuffer sbOuter = new StringBuffer();
        SurfaceResultStruct resultStruct = new SurfaceResultStruct();
        resultStruct.resultBuffer = sbOuter;


        sbOuter.append("<uri>" + uri + "</uri>\r\n");
        sbOuter.append("<csid>" + itemcsid + "</csid>\r\n");

        StringBuffer sb = new StringBuffer();

        String name = "";
        String otherNames="";
        String number = "";
        String otherNumbers = "";

        sb.append("<parents>\r\n");
        if (parentList.size()==0){
            resultStruct.noParents = true;
        }
        
        for (RelationsCommonList.RelationListItem item : parentList) {
            resultStruct.noParents = false;
            RelationsDocListItem parent = item.getObject();
            RelationsDocListItem child = item.getSubject();
            String parentCSID =parent.getCsid();
            String parentURI = parent.getUri();

            String aName = child.getName();
            String aNumber = child.getNumber();
            if (name.length()>0 && (!name.equals(aName))){
                otherNames = otherNames+";"+aName;
            } else {
                name = aName;
            }
            if (number.length()>0 && (!number.equals(aNumber))){
                otherNumbers = otherNumbers+";"+aNumber;
            } else {
                number = aName;
            }

            sb.append("<parent>\r\n");
            //sb.append("<parent-uri>" +parentURI + "</parent-uri>\r\n");

            SurfaceResultStruct struct = surface(ctx, parentCSID, parentURI, false);
            StringBuffer surfaceResult = struct.resultBuffer;

            if (struct.noParents){
                //when there are no more parents, there is no way to look up the name and number, so use this trick:
                sb.append("<name>" + parent.getName() + "</name><number>" + parent.getNumber() + "</number>\r\n");
            }

            sb.append(surfaceResult);
            sb.append("</parent>\r\n");
        }
        sb.append("</parents>\r\n");


        if (Tools.notBlank(name))sbOuter.append("  <name>" +name + "</name>\r\n");
        if (Tools.notBlank(otherNames))    sbOuter.append("  <name-mismatches-by-parents>" +otherNames + "</name-mismatches-by-parents>\r\n");

        if (Tools.notBlank(number)) sbOuter.append("<number>" +number + "</number>\r\n");
        if (Tools.notBlank(otherNumbers)) sbOuter.append("  <number-mismatches-by-parents>" +otherNumbers + "</number-mismatches-by-parents>\r\n");

        sbOuter.append(sb);

        return resultStruct;
    }
}
