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