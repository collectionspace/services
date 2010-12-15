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

package org.collectionspace.services.IntegrationTests.xmlreplay;

import org.collectionspace.services.common.Tools;

import java.util.ArrayList;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class TreeWalkResults extends ArrayList<TreeWalkResults.TreeWalkEntry> {

    public static class TreeWalkEntry {
        public String lpath = "";
        public String rpath = "";
        public String ltextTrimmed = "";
        public String rtextTrimmed = "";
        public String message = "";
        public String errmessage = "";
        public static enum STATUS {INFO, MATCHED, R_MISSING, R_ADDED, DOC_ERROR, TEXT_DIFFERENT};
        public STATUS status;
        public String toString(){
            return
                 "{"
                 +status.name()
                 +(Tools.notEmpty(lpath) ? ", L.path:"+lpath : "")
                 +(Tools.notEmpty(rpath) ? ", R.path:"+rpath : "")
                 +(Tools.notEmpty(message) ? ", message:"+message : "")
                 +((status != STATUS.MATCHED) && Tools.notEmpty(ltextTrimmed) ? ",\r\n    L.trimmed:"+ltextTrimmed : "")
                 +((status != STATUS.MATCHED) && Tools.notEmpty(rtextTrimmed) ? ",\r\n    R.trimmed:"+rtextTrimmed : "")
                 +"}";

        }
    }

    public boolean hasDocErrors(){
        for (TreeWalkEntry entry : this){
            if (entry.status == TreeWalkEntry.STATUS.DOC_ERROR){
                return true;
            }
        }
        return false;
    }

    public String getErrorMessages(){
        StringBuffer buf = new StringBuffer();
        boolean first = true;
        for (TreeWalkEntry entry : this){
            if ( Tools.notEmpty(entry.errmessage)){
                if (first) {
                    buf.append(",errors:");
                } else {
                    buf.append(',');
                }
                buf.append('\''+entry.errmessage+"\'");
                first = false;
            }
        }
        return buf.toString();
    }



    public boolean isStrictMatch(){
        for (TreeWalkEntry entry : this){
            if (entry.status == TreeWalkEntry.STATUS.DOC_ERROR){
                return false;
            }
            if ( !(   entry.status == TreeWalkEntry.STATUS.MATCHED
                   || entry.status == TreeWalkEntry.STATUS.INFO)){
                return false;
            }
        }
        return true;
    }
    public int getMismatchCount(){
        int c = 0;
        for (TreeWalkEntry entry : this){
            if ( entry.status == TreeWalkEntry.STATUS.DOC_ERROR
                || entry.status != TreeWalkEntry.STATUS.MATCHED
                || entry.status != TreeWalkEntry.STATUS.INFO){
                c++;
            }
        }
        return c;
    }
    /** For our purposes, trees match if they have the same element tree structure - no checking is done for text node changes. */
    public boolean treesMatch(){
        for (TreeWalkEntry entry : this){
            if (entry.status == TreeWalkEntry.STATUS.DOC_ERROR
                || entry.status == TreeWalkEntry.STATUS.R_MISSING
                || entry.status == TreeWalkEntry.STATUS.R_ADDED  ){
                return false;
            }
        }
        return true;
    }

    public int countFor(TreeWalkEntry.STATUS status){
        int count = 0;
        for (TreeWalkEntry entry : this){
            if (entry.status.equals(status)){
                count++;
            }
        }
        return count;
    }

    public String miniSummary(){
        //MATCHED, INFO, R_MISSING, R_ADDED, TEXT_DIFFERENT};
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        boolean nextline = false;
        for (TreeWalkEntry.STATUS st : TreeWalkEntry.STATUS.values()){
            if (nextline) buf.append(',');
            buf.append(st.name()+':'+countFor(st));
            nextline = true;
        }
        buf.append(getErrorMessages());
        buf.append("}");
        return buf.toString();
    }

    public String fullSummary(){
        StringBuffer buf = new StringBuffer();
        for (TreeWalkResults.TreeWalkEntry entry : this){
            buf.append(entry.toString()).append("\r\n");
        }
        return buf.toString();
    }


    public String leftID;
    public String rightID;
}