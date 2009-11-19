package org.collectionspace.services.id.part;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class GregorianDateIDPart extends DateIDPart {

    private GregorianDateIDPartOutputFormatter formatter;
    private IDPartValidator validator = IDPart.DEFAULT_VALIDATOR;

    // Because the output from newID() is dependent on a
    // format pattern having been set in the outputFormatter,
    // constructors for this class all require that pattern.

    public GregorianDateIDPart(String formatPattern) {
        setOutputFormatter(new GregorianDateIDPartOutputFormatter(formatPattern));
    }

    public GregorianDateIDPart(String formatPattern, IDPartValidator validator) {
        setOutputFormatter(new GregorianDateIDPartOutputFormatter(formatPattern));
        setValidator(validator);
    }

    public GregorianDateIDPart(String formatPattern, String languageCode) {
        setOutputFormatter(
            new GregorianDateIDPartOutputFormatter(formatPattern, languageCode));
    }

    public GregorianDateIDPart(String formatPattern, String languageCode,
        IDPartValidator validator) {
        setOutputFormatter(
            new GregorianDateIDPartOutputFormatter(formatPattern, languageCode));
        setValidator(validator);
    }

    @Override
    public IDPartOutputFormatter getOutputFormatter() {
       return this.formatter;
    }

    private void setOutputFormatter(GregorianDateIDPartOutputFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public IDPartValidator getValidator() {
        return this.validator;
    }

    private void setValidator(IDPartValidator validator) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String newID() {
        
        // Get the current time instance.
		Calendar cal = GregorianCalendar.getInstance();
        
        // @TODO Implement time zone awareness.
        //
        // Relevant time zones may include:
        // - A time zone for the server environment.
        // - A default time zone for the tenant.
        // - A user-specific time zone, acquired externally
        //   (e.g. from a client when called from that context).
        // cal.setTimeZone(TimeZone tz);

        // The following value is coerced to a String, representing
        // milliseconds in the Epoch, for conformance with the contract
        // that output formatters act on Strings.
        //
        // In the formatter, this value is converted back into a date
        // and date-specific formatting is applied.
        return getOutputFormatter().format(Long.toString(cal.getTime().getTime()));
    }

}

