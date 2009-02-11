package org.kuali.student.contract;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import java.util.regex.Pattern;

public class ContractReader {

    private String contractText;

    public ContractReader(File file) throws FileNotFoundException, IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileReader);

        contractText = trimContract(reader);
    }

    public ContractReader(URL url, String jsessionId) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("Cookie", "JSESSIONID=" + jsessionId);

        InputStreamReader myReader = new InputStreamReader(connection.getInputStream());
        BufferedReader reader = new BufferedReader(myReader);

        contractText = trimContract(reader);
    }

    public Document getDocument() throws ParserConfigurationException, UnsupportedEncodingException, IOException, SAXException {
        DocumentBuilderFactory  factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        return builder.parse(new ByteArrayInputStream(contractText.getBytes("UTF-8")));
    }
    
    public StreamSource getStreamSource() {
        StringReader stringReader = new StringReader(contractText);

        return new StreamSource(stringReader);
    }

    protected String trimContract(BufferedReader reader) throws IOException {

    	String result = null;
        StringBuilder builder = new StringBuilder();
        String line;
        boolean inContract = false;

        while ((line = reader.readLine()) != null) {
//        	System.out.println(line);
            if (!inContract) {
                if (line.contains("<em>Setup</em>")) {
                    inContract = true;
                    builder.append("<contents>");
                }
            } else {
//                if (line.contains("</a>Capabilities</h3>")) {
            	if (Pattern.matches(".*</a>\\s*Capabilities\\s*</h3>.*", line) == true) {
                    inContract = false;
                } else {
                    builder.append(line);
                }
            }
        }

        builder.append("</contents>" + "\n");
        result = builder.toString();
        
        System.out.print(result);
        
        return result;
    }

	/**
	 * @return the contractText
	 */
	public String getContractText() {
		return contractText;
	}
}
