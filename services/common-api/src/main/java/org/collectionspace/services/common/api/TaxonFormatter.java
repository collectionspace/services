package org.collectionspace.services.common.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.gbif.api.model.checklistbank.ParsedName;
import org.gbif.nameparser.NameParser;
import org.gbif.nameparser.UnparsableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaxonFormatter {
    private static final Logger logger = LoggerFactory.getLogger(TaxonFormatter.class);
    private static final Pattern HYBRID_PATTERN = Pattern.compile("^(.*?)( [×xX] )(.*)$");
    
    private NameParser nameParser;
    
    public TaxonFormatter() {
    	this.nameParser = new NameParser();
    }
    
	public String format(String name) {
		if (StringUtils.isBlank(name)) {
			return name;
		}

		Matcher hybridMatcher = HYBRID_PATTERN.matcher(name);
		
		if (hybridMatcher.matches()) {
			String parentName1 = hybridMatcher.group(1);
			String separator = hybridMatcher.group(2);
			String parentName2 = hybridMatcher.group(3);
			
			logger.debug("hybrid formula: parentName1=" + parentName1 + " parentName2=" + parentName2);
			
			return (format(parentName1) + separator + format(parentName2));
		}
		
		ParsedName parsedName = null;

		try {
			parsedName = nameParser.parse(name);
		}
		catch (UnparsableException e) {
			logger.error("error parsing name: name=" + name + " message=" + e.getMessage());
		}

		if (parsedName != null) {
			String genusOrAbove = parsedName.getGenusOrAbove();
			String specificEpithet = parsedName.getSpecificEpithet();
			String infraSpecificEpithet = parsedName.getInfraSpecificEpithet();
			
			logger.debug("parsed name: genusOrAbove=" + genusOrAbove + " specificEpithet=" + specificEpithet + " infraSpecificEpithet=" + infraSpecificEpithet);
			
			if (StringUtils.isNotBlank(genusOrAbove)) {
				name = italicize(name, genusOrAbove);
			}
			
			if (StringUtils.isNotBlank(specificEpithet)) {
				name = italicize(name, specificEpithet);
			}
			
			if (StringUtils.isNotBlank(infraSpecificEpithet)) {
				name = italicize(name, infraSpecificEpithet);
			}	
		}

		return name;
	}
	
	private String italicize(String string, String substring) {
		return Pattern.compile(substring, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.LITERAL).matcher(string).replaceAll("<span style=\"font-style: italic\">$0</span>");
	}
}
