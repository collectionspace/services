package org.kuali.student.contract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class MessageContractReader extends ContractReader {

	/**
	 * @param url
	 * @throws IOException
	 */
	public MessageContractReader(URL url, String jsessionId) throws IOException {
		super(url, jsessionId);
		// TODO Auto-generated constructor stub
	}

	public MessageContractReader(File file) throws FileNotFoundException,
			IOException {
		super(file);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.kuali.student.contract.ContractReader#trimContract(java.io.BufferedReader)
	 */
	@Override
	protected String trimContract(BufferedReader reader) throws IOException {
		StringBuilder builder = new StringBuilder();
		String line;

		// Add in xml header and entity definitions
		builder.append("<?xml version=\"1.0\"?>" + "<!DOCTYPE xsl:stylesheet ["
				+ "<!ENTITY nbsp '&#160;'>" + "]>");

		// Get rid of the first line (doctype info stuff)
		reader.readLine();

		// read each line and fix any open tags, bad attributes, and '&' symbols
		// without a ';'
		while ((line = reader.readLine()) != null) {

			// Check if the current line has a tag that does not end yet
			if (line.matches(".*<[^>]+")) {

				// Concatenate the next lines until the tag is closed
				String newLine;
				while ((newLine = reader.readLine()) != null
						&& !(line += newLine).contains(">")) {
				}
			}

			// Do some regex to clean up the tags
			line = line.replaceAll("border=0", "border=\"0\"");
			line = line.replaceAll("([^:])nowrap([^=])", "$1nowrap=\"true\"$2");
			line = line.replaceAll("&(\\w+[^;])", "$1");
			line = line.replaceAll("(<(META|meta|br|hr|col|link|img|input)(\\s+[\\w-]+\\s*=\\s*(\"([^\"]*)\"|'([^']*)'))*\\s*)>", "$1/>");
			line = line.replaceAll("<div \">", "<div>");
			builder.append(line + "\n");
		}

		return builder.toString();
	}

}
