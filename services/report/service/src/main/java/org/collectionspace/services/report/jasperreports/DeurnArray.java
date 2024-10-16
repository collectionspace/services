package org.collectionspace.services.report.jasperreports;

import java.sql.Array;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * A java.sql.Array of deurned refnames. Only supports varchar arrays.
 */
public class DeurnArray implements Array {

    private final String[] array;

    public DeurnArray(final List<String> array) {
        this.array = array.toArray(new String[0]);
    }

    @Override
    public String getBaseTypeName() {
        return "varchar";
    }

    @Override
    public int getBaseType() {
        return 12;
    }

    @Override
    public Object getArray() {
        return array;
    }

    @Override
    public Object getArray(Map<String, Class<?>> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getArray(long index, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getArray(long index, int count, Map<String, Class<?>> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getResultSet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getResultSet(Map<String, Class<?>> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getResultSet(long index, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getResultSet(long index, int count, Map<String, Class<?>> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void free() {
        // no-op
    }
}
