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
 * PreparedStatementBuilder
 * 
 * Simple workaround for the inability to create a JDBC
 * PreparedStatement without having a current Connection
 * 
 * See http://stackoverflow.com/a/7127189
 * and http://blog.stackoverflow.com/2009/06/stack-overflow-creative-commons-data-dump/
 */

package org.collectionspace.services.common.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementBuilder
{
    private String sql;

    public PreparedStatementBuilder(final String sql) {
        this.sql = sql;
    }

   /**
    * A 'virtual' method to be overridden, in which to declare setup directives
    * to be applied to a PreparedStatement; for instance, to add values to
    * replaceable parameters or otherwise modify the statement's behavior.
    * 
    * (The PreparedStatement will not yet exist at the time this method is overridden.)
    * 
    * @param preparedStatement a JDBC PreparedStatement.
    * @throws SQLException 
    */
    protected void preparePrepared(final PreparedStatement preparedStatement) 
            throws SQLException {
    }

    /**
     * Build a PreparedStatement by obtaining it from a JDBC Connection,
     * then applying setup directives.
     * 
     * @param conn a JDBC connection
     * @return a JDBC PreparedStatement, with any 
     * @throws SQLException 
     */
    public PreparedStatement build(final Connection conn)
            throws SQLException
    {
        final PreparedStatement returnable = conn.prepareStatement(sql);
        preparePrepared(returnable);
        return returnable;
    }
}