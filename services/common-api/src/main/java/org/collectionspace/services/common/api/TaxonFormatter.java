package org.collectionspace.services.common.api;

import org.apache.commons.lang3.StringUtils;
import org.gbif.api.model.checklistbank.ParsedName;
import org.gbif.nameparser.NameParser;
import org.gbif.nameparser.UnparsableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaxonFormatter {
    private static final Logger logger = LoggerFactory.getLogger(TaxonFormatter.class);

    private NameParser nameParser;
    
    public TaxonFormatter() {
    	this.nameParser = new NameParser();
    }
    
	public String format(String name) {
		try {
			ParsedName parsedName = nameParser.parse(name);
			
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
		catch (UnparsableException e) {
			logger.error("error parsing name: name=" + name + " message=" + e.getMessage());
		}

		return name;
	}
	
	private String italicize(String string, String substring) {
		return string.replaceAll(substring, "<span style=\"font-style: italic\">" + substring + "</span>");
	}
}
