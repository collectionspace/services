package org.collectionspace.services.common.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
/**
 * Per http://stackoverflow.com/a/7127189
 */

import java.sql.SQLException;

public class PreparedStatementBuilder
{
    private String sql;

    public PreparedStatementBuilder(final String sql) {
        this.sql = sql;
    }

    protected void preparePrepared(final PreparedStatement preparedStatement) 
            throws SQLException {
        // This virtual method lets us declare how, when we generate our
        // PreparedStatement, we want it to be set up.

        // Note that at the time this method is overridden, the 
        // PreparedStatement has not yet been created.
    }

    public PreparedStatement build(final Connection conn)
            throws SQLException
    {
        // Fetch the PreparedStatement
        final PreparedStatement returnable = conn.prepareStatement(sql);
        // Perform setup directives
        preparePrepared(returnable);
        return returnable;
    }
}