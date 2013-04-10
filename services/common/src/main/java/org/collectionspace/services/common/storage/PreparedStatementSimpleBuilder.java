/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright © 2009-2013 The Regents of the University of California 

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

/**
 * PreparedStatementSimpleBuilder
 * 
 * Specialization of PreparedStatementBuilder that simply applies a
 * set of String values, in order, to each of the replaceable parameters
 * in a PreparedStatement.
 */

package org.collectionspace.services.common.storage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PreparedStatementSimpleBuilder extends PreparedStatementBuilder {

    private List<String> params;

    public PreparedStatementSimpleBuilder(final String sql, final List<String> params) {
        super(sql);
        this.params = params;
    }

    @Override
    protected void preparePrepared(final PreparedStatement preparedStatement)
            throws SQLException {
        int i = 0;
        for (String param : params) {
            i++;
            preparedStatement.setString(i, param);
        }
    }
}