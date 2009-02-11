package org.kuali.student.mojo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.kuali.student.contract.ContractReader;
import org.kuali.student.contract.MessageContractReader;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Creates WS service interface from wiki service contract.
 * 
 * @goal contractToJava
 */
public class KsContractMojo extends AbstractMojo {

	public KsContractMojo(
			File contractFile,
			URL contractURL, 
			File outputDirectory,
			File transformFile,
			File messageTransformFile,
			String jsessionId)
	{
		this.contractFile = contractFile;
		this.contractURL = contractURL;
		this.outputDirectory = outputDirectory;
		this.transformFile = transformFile;
		this.messageTransformFile = messageTransformFile;
		this.jsessionId = jsessionId;
		
		return;
	}
	
	public KsContractMojo() {
		this.contractFile = null;
		this.contractURL = null;
	}

	/**
	 * Path to service contract file.
	 * 
	 * @parameter
	 */
	private File contractFile;

	/**
	 * URL of service contract from wiki.
	 * 
	 * @parameter
	 */
	private URL contractURL;

	/**
	 * Path to output directory.
	 * 
	 * @parameter
	 */
	private File outputDirectory;

	/**
	 * Path to custom xslt.
	 * 
	 * @parameter
	 */
	private File transformFile;

	/**
	 * Path to custom xslt.
	 * 
	 * @parameter
	 */
	private File messageTransformFile;
	
	/**
	 * JSESSIONID from a current session
	 * 
	 * @parameter
	 */
	private String jsessionId;
	
	public void execute() throws MojoExecutionException, MojoFailureException {

		ContractReader contract;

		try {
			if (contractURL != null) {
				contract = new ContractReader(contractURL, jsessionId);
			} else {
				contract = new ContractReader(contractFile);
			}
		} catch (Exception ex) {
			throw new MojoExecutionException("Can't parse contract", ex);
		}

		StreamSource wsdlXslt;
		if (transformFile == null) {
			wsdlXslt = new StreamSource(this.getClass().getClassLoader()
					.getResourceAsStream("interface.xml"));
		} else {
			wsdlXslt = new StreamSource(transformFile);
		}

		try {
			Transformer transformer = net.sf.saxon.TransformerFactoryImpl
					.newInstance().newTransformer(wsdlXslt);

			Result result = new StreamResult(new File(outputDirectory,
					"foo.java"));
			transformer.transform(contract.getStreamSource(), result);

			findParams(contract.getDocument());
		} catch (TransformerConfigurationException tcex) {
			getLog().error(tcex);
			throw new MojoExecutionException("Can't initialize xslt.", tcex);
		} catch (TransformerException tex) {
			getLog().error(tex);
			throw new MojoExecutionException("Can't transform contract.", tex);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new MojoExecutionException("Error parsing params", ex);
		}
	}

	public void findParamsFromMessage(String param, URL url,
			Set<URL> alreadyParsedUrlSet) {
		if (alreadyParsedUrlSet.contains(url) || "datetime".equals(param.toLowerCase())
				|| "string".equals(param.toLowerCase()) || "boolean".equals(param.toLowerCase())
				|| "integer".equals(param.toLowerCase())) {
			return;
		}
		ContractReader contract;
		try {
			contract = new MessageContractReader(url, jsessionId);
			// Create the java code
			if (!param.endsWith("List") && !param.endsWith("Id")
					&& !param.endsWith("Key") && !param.endsWith("Type")) {
				message2Java(contract, param);
			}
			// add this url to the already oarsed list
			alreadyParsedUrlSet.add(url);

			// recurse through the node to
			NodeList nodeList = contract.getDocument().getElementsByTagName(
					"td");
			for (int i = 0, iCnt = nodeList.getLength(); i < iCnt; i++) {
				Node node = nodeList.item(i);
				NamedNodeMap nodeMap = node.getAttributes();
				if (nodeMap != null
						&& nodeMap.getNamedItem("class") != null
						&& "structType".equals(nodeMap.getNamedItem("class")
								.getNodeValue())) {
					Node anchor = node.getFirstChild();
					String urlString = anchor.getAttributes().getNamedItem(
							"href").getNodeValue();
					if (!urlString.startsWith("http")
							&& !urlString.startsWith("file")) {
						urlString = contractURL.getProtocol()+"://"+contractURL.getHost()
								+ urlString;
					}
					URL newUrl = new URL(urlString);
					String newParam = anchor.getTextContent().trim();
					if (!newParam.startsWith("<")
							&& !alreadyParsedUrlSet.contains(newUrl)) {
						findParamsFromMessage(newParam, newUrl,
								alreadyParsedUrlSet);
					}
				}
			}
		} catch (IOException e) {
			getLog().warn(
					"Error loading page for Type '" + param + "'"
							+ e.getMessage());
		} catch (ParserConfigurationException e) {
			getLog().warn(
					"Error parsing page for Type '" + param + "'"
							+ e.getMessage());
		} catch (SAXException e) {
			getLog().warn(
					"Error parsing page for Type '" + param + "'"
							+ e.getMessage());
		}
	}

	public void findParams(Document document) {
		Set<URL> alreadyParsedUrlSet = new HashSet<URL>();
		NodeList nodeList = document.getElementsByTagName("td");
		for (int i = 0, iCnt = nodeList.getLength(); i < iCnt; i++) {
			Node node = nodeList.item(i);
			NamedNodeMap nodeMap = node.getAttributes();
			if (nodeMap != null
					&& ("methodParamType".equals(nodeMap.getNamedItem("class")
							.getNodeValue()) || "methodReturnType"
							.equals(nodeMap.getNamedItem("class")
									.getNodeValue()))) {
				String param = node.getFirstChild().getTextContent().trim();
				if (!"None".equals(param) && !param.startsWith("<")
						&& !param.endsWith("Id") && !param.endsWith("Key")) {
					try {
						String urlString = node.getFirstChild().getAttributes()
								.getNamedItem("href").getNodeValue();
						System.out.println("urlString:" + urlString);
						System.out.println("contractURL:" + contractURL);
						if (!urlString.startsWith("http")
								&& !urlString.startsWith("file")) {
							urlString = contractURL.getProtocol()+"://"+contractURL.getHost()
									+ urlString;
						}
						findParamsFromMessage(param, new URL(urlString),
								alreadyParsedUrlSet);
					} catch (MalformedURLException e) {
						getLog().warn(
								"Error loading page for Type '" + param + "'"
										+ e.getMessage());
					} catch (DOMException e) {
						getLog().warn(
								"DOM Error parsing page for Type '" + param
										+ "'" + e.getMessage());
					}
				}
			}
		}
	}

	private void message2Java(ContractReader contract, String param) {
		StreamSource wsdlXslt;
		if (messageTransformFile == null) {
			wsdlXslt = new StreamSource(this.getClass().getClassLoader()
					.getResourceAsStream("messageInterface.xml"));
		} else {
			wsdlXslt = new StreamSource(messageTransformFile);
		}
		try {
			Transformer transformer = net.sf.saxon.TransformerFactoryImpl
					.newInstance().newTransformer(wsdlXslt);

			Result result = new StreamResult(new File(outputDirectory, param
					.substring(0, 1).toUpperCase()
					+ param.substring(1) + ".java"));
			transformer.transform(contract.getStreamSource(), result);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public File getContractFile() {
		return contractFile;
	}

	public void setContractFile(File contractFile) {
		this.contractFile = contractFile;
	}

	public URL getContractURL() {
		return contractURL;
	}

	public void setContractURL(URL contractURL) {
		this.contractURL = contractURL;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public File getTransformFile() {
		return transformFile;
	}

	public void setTransformFile(File transformFile) {
		this.transformFile = transformFile;
	}

	/**
	 * @return the messageTransformFile
	 */
	public File getMessageTransformFile() {
		return messageTransformFile;
	}

	/**
	 * @param messageTransformFile
	 *            the messageTransformFile to set
	 */
	public void setMessageTransformFile(File messageTransformFile) {
		this.messageTransformFile = messageTransformFile;
	}

}
