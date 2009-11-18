package org.collectionspace.services.id.part;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IDPartRegexValidator implements IDPartValidator {

    final Logger logger = LoggerFactory.getLogger(IDPartRegexValidator.class);

    @Override
    public boolean isValid(String id) {

        if (id == null) {
            return false;
        }

        boolean isValid = false;
        try {
            Pattern pattern = Pattern.compile(getRegexPattern());
            Matcher matcher = pattern.matcher(id);
            if (matcher.matches()) {
                isValid = true;
            }
        // @TODO Validation will fail by default if the regex pattern
        // cannot be compiled.  We may wish to consider re-throwing this
        // Exception as an IllegalStateException, to raise this issue for
        // timely resolution.
        } catch (PatternSyntaxException e) {
            String regex = getRegexPattern();
            String msg =
                (regex == null || regex.trim().isEmpty()) ?
                "Could not validate ID due to null or empty regex pattern." :
                "Could not validate ID due to invalid regex pattern: " + regex;
            logger.error(msg, e);
        }

        return isValid;

    }

    public abstract String getRegexPattern();

}
