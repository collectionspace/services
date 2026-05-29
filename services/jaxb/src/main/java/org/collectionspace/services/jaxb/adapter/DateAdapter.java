package org.collectionspace.services.jaxb.adapter;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter to convert {@link Date} to an ISO-8601 String at UTC-0
 *
 * @since 9.0.0
 */
public class DateAdapter extends XmlAdapter<String, Date> {
    @Override
    public Date unmarshal(String s) {
        return Date.from(Instant.parse(s));
    }

    @Override
    public String marshal(Date date) {
        return DateTimeFormatter.ISO_INSTANT.format(date.toInstant());
    }
}
