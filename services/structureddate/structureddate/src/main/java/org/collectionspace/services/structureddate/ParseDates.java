package org.collectionspace.services.structureddate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;

public class ParseDates {

	/**
	 * Parse a newline-separated list of strings from a file (or standard input),
	 * and print the results to standard output.
	 *
	 * @param args The first argument to the program is the name of the file
	 *             containing strings to parse. If not supplied, strings are
	 *             read from standard input.
	 */
	public static void main(String[] args) {
		BufferedReader in = null;

		if (args.length > 0) {
			String filename = args[0];

			try {
				in = new BufferedReader(new FileReader(filename));
			} catch (FileNotFoundException e) {
				System.err.println("File not found: " + filename);
			}
		}
		else {
			in = new BufferedReader(new InputStreamReader(System.in));
		}

		if (in == null) {
			return;
		}

		try {
			for(String line; (line = in.readLine()) != null; ) {
				line = StringUtils.trim(line);

				if (StringUtils.isNotEmpty(line)) {
					parse(line);
				}
			}
		}
		catch(IOException e) {
			System.err.println("Error reading file: " + e.getLocalizedMessage());
		}

		try {
			in.close();
		}
		catch(IOException e) {
			System.err.println("Error closing file: " + e.getLocalizedMessage());
		}
	}

	private static void parse(String displayDate) {
		System.out.print(displayDate + "\t");

		String result = "";
		String scalar = "";

		try {
			StructuredDateInternal structuredDate = StructuredDateInternal.parse(displayDate);
			Date earliestSingleDate = structuredDate.getEarliestSingleDate();
			Date latestDate = structuredDate.getLatestDate();

			result =
				earliestSingleDate.getYear() + "-" +
				earliestSingleDate.getMonth() + "-" +
				earliestSingleDate.getDay() + " " +
				earliestSingleDate.getEra().toDisplayString(); // use toString() to get the data value (refname)

				// These don't get filled in by the parser, so no need to print.

				// earliestSingleDate.getCertainty();
				// earliestSingleDate.getQualifierType();
				// earliestSingleDate.getQualifierValue();
				// earliestSingleDate.getQualifierUnit();
				// earliestSingleDate.getScalarValue();

			if (latestDate != null) {
				result += " - " +
					latestDate.getYear() + "-" +
					latestDate.getMonth() + "-" +
					latestDate.getDay() + " " +
					latestDate.getEra().toDisplayString(); // use toString() to get the data value (refname)
			}

			try {
				structuredDate.computeScalarValues();

				scalar = structuredDate.getEarliestScalarValue() + " - " + structuredDate.getLatestScalarValue();
			}
			catch(InvalidDateException e) {
				scalar = "[invalid date: " + e.getMessage() + "]";
			}
		}
		catch(StructuredDateFormatException e) {
			result = "[unable to parse]";
			scalar = "";
		}

		System.out.println(result + "\t" + scalar);
	}
}
