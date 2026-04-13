package org.collectionspace.services.jaxb.adapter;

import java.time.Instant;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DateAdapterTest {

    private static final String STRING_EPOCH = "1970-01-01T00:00:00Z";
    private static final Date DATE_EPOCH = Date.from(Instant.EPOCH);
    private final DateAdapter adapter = new DateAdapter();

    @Test
    public void testUnmarshal() {
        Date unmarshalled = adapter.unmarshal(STRING_EPOCH);
        Assert.assertEquals(unmarshalled, DATE_EPOCH);
    }

    @Test
    public void testMarshal() {
        String marshalled = adapter.marshal(DATE_EPOCH);
        Assert.assertEquals(marshalled, STRING_EPOCH);
    }
}