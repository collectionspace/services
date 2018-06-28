package org.collectionspace.services.common.api;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.gbif.nameparser.api.ParsedName;
import org.gbif.nameparser.NameParserGBIF;
import org.gbif.nameparser.api.UnparsableNameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaxonFormatter {
    private static final Logger logger = LoggerFactory.getLogger(TaxonFormatter.class);
    private static final Pattern HYBRID_FORMULA_PATTERN = Pattern.compile("^(.*?)(\\s[×xX]\\s)(.*)$");
    private static final Pattern BROKEN_HYBRID_FORMULA_PATTERN = Pattern.compile("^×\\s*|\\s*×$");
    private static final Pattern ADJACENT_ITALIC_TAG_PATTERN = Pattern.compile("</i>(\\s*)<i>");
    private static final Pattern STARTS_WITH_INFRASPECIFIC_RANK_PATTERN = Pattern.compile("^\\s*(var|subsp|cv|aff)\\.");
    private static final String SUBSPECIES_QUALIFIER_MARKER_REGEXP = "(section|subsection|ser\\.|sser\\.)";
    private static final Pattern SUBSPECIES_WITH_QUALIFIER_PATTERN = Pattern.compile("(\\s|^)(subsp\\.\\s+)" + SUBSPECIES_QUALIFIER_MARKER_REGEXP + "(\\s)(.*?)(\\s|$)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern PARENTHESIZED_SUBSPECIES_WITH_QUALIFIER_PATTERN = Pattern.compile("(\\s|^)(subsp\\.\\s+)\\(" + SUBSPECIES_QUALIFIER_MARKER_REGEXP + "(.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern FAMILY_NAME_PATTERN = Pattern.compile("^[A-Z]+$");

    private NameParserGBIF nameParser;

    public TaxonFormatter() {
    	this.nameParser = new NameParserGBIF();
    }

	public String format(String name) {
		if (StringUtils.isBlank(name)) {
			return name;
		}

		if (FAMILY_NAME_PATTERN.matcher(name).matches()) {
			// Per Barbara Keller, family names are never italicized.
			return name;
		}

		Matcher hybridMatcher = HYBRID_FORMULA_PATTERN.matcher(name);

		if (hybridMatcher.matches()) {
			String parentName1 = hybridMatcher.group(1);
			String separator = hybridMatcher.group(2);
			String parentName2 = hybridMatcher.group(3);

			logger.info("hybrid formula: parentName1=" + parentName1 + " parentName2=" + parentName2);

			return (format(parentName1) + separator + format(parentName2));
		}

		String normalizedName = name;

		if (BROKEN_HYBRID_FORMULA_PATTERN.matcher(normalizedName).find()) {
			logger.info("broken hybrid: name=" + name + " normalizedName=" + normalizedName);

			normalizedName = BROKEN_HYBRID_FORMULA_PATTERN.matcher(normalizedName).replaceAll("");
			logger.info("normalized to:" + normalizedName);
		}

		if (PARENTHESIZED_SUBSPECIES_WITH_QUALIFIER_PATTERN.matcher(normalizedName).find()) {
			logger.info("parenthesized qualified subspecies: name=" + name + " normalizedName=" + normalizedName);

			normalizedName = PARENTHESIZED_SUBSPECIES_WITH_QUALIFIER_PATTERN.matcher(normalizedName).replaceFirst("$1$2$3$4");
			logger.info("normalized to:" + normalizedName);
		}

		Matcher subspeciesWithQualifierMatcher = SUBSPECIES_WITH_QUALIFIER_PATTERN.matcher(normalizedName);

		if (subspeciesWithQualifierMatcher.find()) {
			logger.info("qualified subspecies: name=" + name + " normalizedName=" + normalizedName);

			MatchResult matchResult = subspeciesWithQualifierMatcher.toMatchResult();

			// Remove the qualifier (e.g. section, ser., sser.). In some data from SAGE, the latin name
			// following the qualifier is capitalized, which the GBIF parser won't handle, so lowercase it.
			String replacement = matchResult.group(1) + matchResult.group(2) + matchResult.group(5).toLowerCase() + matchResult.group(6);
			normalizedName = normalizedName.substring(0, matchResult.start()) + replacement + normalizedName.substring(matchResult.end());
			logger.info("normalized to:" + normalizedName);
		}

		if (STARTS_WITH_INFRASPECIFIC_RANK_PATTERN.matcher(normalizedName).find()) {
			/*
			 * There are some non-standard taxon names in SAGE data, where there is an infraspecific rank/epithet, but no genus/species, e.g.
			 *     subsp. occidentalis (J.T. Howell) C.B. Wolf
			 *
			 * Since the GBIF parser can't handle this, we'll temporarily prepend an arbitrary genus and species for parsing purposes.
			 */
			logger.info("name starts with infraspecific rank: name=" + name + " normalizedName=" + normalizedName);

			normalizedName = "Tempgenus tempspecies " + normalizedName;
			logger.info("normalized to:" + normalizedName);
		}

		ParsedName parsedName = null;

		try {
			parsedName = nameParser.parse(normalizedName);
		}
		catch (UnparsableNameException e) {
			/*
			 *  Some non-standard taxon names in SAGE data have a species, but no genus. Try to account for these by
			 *  temporarily prepending an arbitrary genus.
			 */

			logger.info("Unparsable name, trying with a temp genus: name=" + name + " normalizedName=" + normalizedName);

			normalizedName = "Tempgenus " + normalizedName;

			try {
				parsedName = nameParser.parse(normalizedName);
			}
			catch (UnparsableNameException ex) {
				logger.error("error parsing name: name=" + name + " normalizedName=" + normalizedName + " message=" + e.getMessage());
			}
		}

		if (parsedName != null) {
			String genusOrAbove = parsedName.getGenus();
			String specificEpithet = parsedName.getSpecificEpithet();
			String infraSpecificEpithet = parsedName.getInfraspecificEpithet();

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

			name = compressTags(name);
		}

		return name;
	}

	private String italicize(String string, String substring) {
		return Pattern.compile("(\\s|\\(|^)(" + Pattern.quote(substring) + ")(\\s|\\)|$)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(string).replaceAll("$1<i>$2</i>$3");
	}

	private String compressTags(String html) {
		html = ADJACENT_ITALIC_TAG_PATTERN.matcher(html).replaceAll("$1");

		return html;
	}
}
